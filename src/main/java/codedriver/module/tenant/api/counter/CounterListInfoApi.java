package codedriver.module.tenant.api.counter;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.counter.dto.GlobalCounterVo;
import codedriver.framework.counter.core.GlobalCounterFactory;
import codedriver.framework.counter.core.IGlobalCounter;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.counter.GlobalCounterService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-10 12:08
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class CounterListInfoApi extends PrivateApiComponentBase {

    @Autowired
    private GlobalCounterService counterService;

    @Override
    public String getToken() {
        return "globalCounter/getCounterInfo";
    }

    @Override
    public String getName() {
        return "查询消息统计信息集合接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(name = "pluginId", type = ApiParamType.LONG, desc="插件ID"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "插件名称"),
            @Param(name = "moduleName", type = ApiParamType.STRING, desc = "模块名称"),
            @Param(name = "moduleIcon", type = ApiParamType.STRING, desc = "模块图标"),
            @Param(name = "data", type = ApiParamType.JSONOBJECT, desc = "插件自定义配置数据"),
            @Param(name = "showTemplate", type = ApiParamType.STRING, desc = "展示模板路径")})
    @Description(desc = "查询消息统计信息集合接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<GlobalCounterVo> counterVoList = counterService.getSubscribeCounterListByUserUuid(UserContext.get().getUserUuid(true));
        JSONArray dataArray = new JSONArray();
        if (counterVoList != null && counterVoList.size() > 0){
            for (GlobalCounterVo counter : counterVoList){
                JSONObject remindObj = new JSONObject();
                IGlobalCounter counterPlugin = GlobalCounterFactory.getCounter(counter.getPluginId());
                Object data = counterPlugin.getShowData();
                remindObj.put("pluginId", counter.getPluginId());
                remindObj.put("name", counterPlugin.getName());
                remindObj.put("moduleNme", counter.getModuleName());
                remindObj.put("moduleIcon", counter.getModuleIcon());
                remindObj.put("data", data);
                remindObj.put("showTemplate", counterPlugin.getShowTemplate());
                dataArray.add(remindObj);
            }
        }
        returnObj.put("counterList", dataArray);
        return returnObj;
    }
}
