package codedriver.module.tenant.api.message;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.message.constvalue.PopUpType;
import codedriver.framework.message.core.IMessageHandler;
import codedriver.framework.message.core.MessageHandlerFactory;
import codedriver.framework.message.dao.mapper.MessageMapper;
import codedriver.framework.message.dto.MessageHandlerVo;
import codedriver.framework.message.exception.MessageHandlerNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Title: MessageHandlerActiveUpdateApi
 * @Package codedriver.module.tenant.api.message
 * @Description: 消息类型订阅接口
 * @Author: linbq
 * @Date: 2020/12/31 14:54
 * Copyright(c) 2020 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class MessageHandlerActiveUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public String getToken() {
        return "message/handler/active/update";
    }

    @Override
    public String getName() {
        return "消息类型订阅";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "消息类型处理器全类名")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.INTEGER, desc = "是否激活")
    })
    @Description(desc = "消息类型订阅")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String handler = jsonObj.getString("handler");
        IMessageHandler messageHandler = MessageHandlerFactory.getHandler(handler);
        if (messageHandler == null) {
            throw new MessageHandlerNotFoundException(handler);
        }
        MessageHandlerVo searchVo = new MessageHandlerVo();
        searchVo.setHandler(handler);
        searchVo.setUserUuid(UserContext.get().getUserUuid(true));
        MessageHandlerVo messageHandlerVo = messageMapper.getMessageSubscribeByUserUuidAndHandler(searchVo);
        if (messageHandlerVo != null) {
            messageHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            messageMapper.updateMessageSubscribeActive(messageHandlerVo);
            return messageHandlerVo.getIsActive() == 0 ? 1 : 0;
        } else {
            messageHandlerVo = new MessageHandlerVo();
            messageHandlerVo.setHandler(handler);
            messageHandlerVo.setIsActive(0);
            messageHandlerVo.setPopUp(PopUpType.CLOSE.getValue());
            messageHandlerVo.setUserUuid(UserContext.get().getUserUuid(true));
            messageMapper.insertMessageSubscribe(messageHandlerVo);
            return 0;
        }
    }
}