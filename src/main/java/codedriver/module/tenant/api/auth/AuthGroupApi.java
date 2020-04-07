package codedriver.module.tenant.api.auth;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.common.constvalue.ModuleEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-17 12:03
 **/
@Service
public class AuthGroupApi extends ApiComponentBase {

    @Override
    public String getToken() {
        return "auth/group";
    }

    @Override
    public String getName() {
        return "权限组列表获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
            @Param( name = "groupList", type = ApiParamType.JSONARRAY, desc = "权限组列表")
    })
    @Description(desc = "权限组列表获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        JSONArray groupArray = new JSONArray();
        Map<String, List<AuthBase>> authGroupMap = AuthFactory.getAuthGroupMap();
        Set<String> groupSet = authGroupMap.keySet();
        for (String group : groupSet){
            JSONObject groupObj = new JSONObject();
            groupObj.put("value", group);
            groupObj.put("text", ModuleEnum.getText(group));
            groupArray.add(groupObj);
        }
        returnObj.put("groupList", groupArray);
        return returnObj;
    }
}