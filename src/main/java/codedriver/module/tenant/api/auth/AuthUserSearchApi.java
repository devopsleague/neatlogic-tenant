package codedriver.module.tenant.api.auth;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-13 12:01
 **/
@Service
public class AuthUserSearchApi extends ApiComponentBase {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "auth/user/search";
    }

    @Override
    public String getName() {
        return "权限用户查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input( {
            @Param( name = "auth",  desc = "权限", type = ApiParamType.STRING, isRequired = true)
    })

    @Output({
            @Param( name = "userList", desc = "用户列表", type = ApiParamType.JSONARRAY, explode = UserVo[].class),
            @Param( name = "roleUserList", desc = "角色用户列表", type = ApiParamType.JSONARRAY, explode = UserVo[].class)
    })

    @Description(desc = "权限用户查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String auth = jsonObj.getString("auth");
        List<UserVo> roleUserList = userMapper.searchRoleUserByAuth(auth);
        Set<String> roleUserSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(roleUserList)){
            for (UserVo user : roleUserList){
                roleUserSet.add(user.getUserId());
            }
        }
        List<UserVo> userList = userMapper.searchUserByAuth(auth);
        if (CollectionUtils.isNotEmpty(userList)){
            Iterator<UserVo> iterator = userList.iterator();
            while (iterator.hasNext()){
                UserVo userVo = iterator.next();
                if (roleUserSet.contains(userVo.getUserId())){
                    iterator.remove();
                }
            }
        }
        returnObj.put("roleUserList", roleUserList);
        returnObj.put("userList", userList);
        return returnObj;
    }
}
