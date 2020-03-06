package codedriver.module.tenant.api.user;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.UserService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-06 11:49
 **/
@Service
public class UserGetListApi extends ApiComponentBase {

    @Autowired
    private UserService userService;

    @Override
    public String getToken() {
        return "user/get/list";
    }

    @Override
    public String getName() {
        return "批量获取用户信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "userIdList", type = ApiParamType.JSONARRAY, desc = "用户id集合", isRequired = true)
    })
    @Output({
            @Param( name = "userList", explode = UserVo[].class, desc = "用户信息集合")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        JSONArray idArray = jsonObj.getJSONArray("userIdList");
        List<UserVo> userList = new ArrayList<>();
        for (int i = 0; i < idArray.size(); i++){
            UserVo userVo = userService.getUserByUserId(idArray.getString(i));
            if (userVo != null){
                userList.add(userVo);
            }
        }
        returnObj.put("userList", userList);
        return returnObj;
    }
}
