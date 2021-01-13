package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.message.constvalue.PopUpType;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.message.dto.MessageSearchVo;
import codedriver.framework.message.dto.MessageVo;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: MessagePullApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 拉取新消息列表接口
 * @Author: linbq
 * @Date: 2021/1/4 15:13
 * Copyright(c) 2020 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MessagePullApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/pull";
    }

    @Override
    public String getName() {
        return "拉取新消息列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "messageId", type = ApiParamType.LONG, desc = "起点消息id"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", explode = MessageVo[].class, desc = "消息列表"),
            @Param(name = "hasSubscription", type = ApiParamType.INTEGER, desc = "是否有订阅消息"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "拉取新消息列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<MessageVo> messageVoList = new ArrayList<>();
        MessageSearchVo searchVo = JSONObject.toJavaObject(jsonObj, MessageSearchVo.class);
        searchVo.setCurrentPage(1);
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        int pageCount = 0;
        int rowNum = messageMapper.getMessageNewCount(searchVo);
        if (rowNum > 0) {
            pageCount = PageUtil.getPageCount(rowNum, searchVo.getPageSize());
            messageVoList = messageMapper.getMessageNewList(searchVo);
            List<MessageHandlerVo> messageSubscribeList = messageMapper.getMessageSubscribeListByUserUuid(UserContext.get().getUserUuid(true));
            Map<String, MessageHandlerVo> messageSubscribeMap = messageSubscribeList.stream().collect(Collectors.toMap(e -> e.getHandler(), e -> e));
            for (MessageVo messageVo : messageVoList) {
                MessageHandlerVo messageHandlerVo = messageSubscribeMap.get(messageVo.getHandler());
                if (messageHandlerVo != null) {
                    messageVo.setPopUp(messageHandlerVo.getPopUp());
                } else {
                    messageVo.setPopUp(PopUpType.CLOSE.getValue());
                }
            }
        }
        resultObj.put("currentPage", searchVo.getCurrentPage());
        resultObj.put("pageSize", searchVo.getPageSize());
        resultObj.put("pageCount", pageCount);
        resultObj.put("rowNum", rowNum);
        resultObj.put("tbodyList", messageVoList);
        List<String> unsubscribeHandlerList = new ArrayList<>();
        List<MessageHandlerVo> messageSubscribeList = messageMapper.getMessageSubscribeListByUserUuid(UserContext.get().getUserUuid(true));
        for(MessageHandlerVo messageHandlerVo : messageSubscribeList){
            if(messageHandlerVo.getIsActive() == 1){
                resultObj.put("hasSubscription", 1);
                return resultObj;
            }
            unsubscribeHandlerList.add(messageHandlerVo.getHandler());
        }
        for (MessageHandlerVo messageHandlerVo : MessageHandlerFactory.getMessageHandlerVoList()) {
            if(!unsubscribeHandlerList.contains(messageHandlerVo.getHandler())){
                resultObj.put("hasSubscription", 1);
                return resultObj;
            }
        }
        resultObj.put("hasSubscription", 0);
        return resultObj;
    }
}
