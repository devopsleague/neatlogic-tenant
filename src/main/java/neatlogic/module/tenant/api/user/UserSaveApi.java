/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.USER_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.dto.UserAuthVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.UserIdRepeatException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.UuidUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
@AuthAction(action = USER_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UserSaveApi extends PrivateApiComponentBase {

    @Resource
    UserMapper userMapper;


    @Override
    public String getToken() {
        return "user/save";
    }

    @Override
    public String getName() {
        return "nmtau.usersaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "common.userid", isRequired = true, xss = true),
            @Param(name = "userName", type = ApiParamType.STRING, desc = "common.username", isRequired = true, xss = true),
            @Param(name = "password", type = ApiParamType.STRING, desc = "common.password", xss = true),
            @Param(name = "email", type = ApiParamType.STRING, desc = "common.email", isRequired = false, xss = true),
            @Param(name = "phone", type = ApiParamType.STRING, desc = "common.phone", isRequired = false, xss = true),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "common.isactive", isRequired = false),
            @Param(name = "teamUuidList", type = ApiParamType.JSONARRAY, desc = "common.teamuuidlist", isRequired = false),
            @Param(name = "roleUuidList", type = ApiParamType.JSONARRAY, desc = "common.roleuuidlist", isRequired = false),
            @Param(name = "userInfo", type = ApiParamType.STRING, desc = "term.framework.userotherinfo", isRequired = false, xss = true),
            @Param(name = "vipLevel", type = ApiParamType.ENUM, desc = "term.framework.viplevel", isRequired = false, rule = "0,1,2,3,4,5"),
            @Param(name = "userAuthList", type = ApiParamType.JSONOBJECT, desc = "common.authlist")
    })
    @Output({})
    @Description(desc = "nmtau.usersaveapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        UserVo userVo = new UserVo();
        userVo.setUserId(jsonObj.getString("userId"));
        userVo.setUserName(jsonObj.getString("userName"));
        userVo.setPassword(jsonObj.getString("password"));
        userVo.setEmail(jsonObj.getString("email"));
        userVo.setPhone(jsonObj.getString("phone"));
        userVo.setIsActive(jsonObj.getInteger("isActive"));
        userVo.setUserInfo(jsonObj.getString("userInfo"));
        userVo.setVipLevel(jsonObj.getInteger("vipLevel"));

        String uuid = jsonObj.getString("uuid");
        if (StringUtils.isBlank(uuid)) {
            userVo.setUuid(UuidUtil.randomUuid());
            if (userMapper.checkUserIdIsIsRepeat(userVo) > 0) {
                throw new UserIdRepeatException(userVo.getUserId());
            }
            UserVo deletedUserVo = userMapper.getUserByUserId(userVo.getUserId());
            if (deletedUserVo != null) {
                // userId是已删除的旧用户，重用用户uuid
                String oldUuid = deletedUserVo.getUuid();
                userMapper.updateUserIsNotDeletedByUuid(oldUuid);
                userVo.setUuid(oldUuid);
                // 删除用户角色
                userMapper.deleteUserRoleByUserUuid(oldUuid);
                // 删除用户组
                userMapper.deleteUserTeamByUserUuid(oldUuid);
                // 删除用户权限
                userMapper.deleteUserAuth(new UserAuthVo(oldUuid));
                // 删除用户密码
                userMapper.deleteUserPasswordByUserUuid(oldUuid);
                // 删除用户个性化中弹窗提醒设置数据
                userMapper.deleteUserDataByUserUuid(oldUuid);
                // 删除用户个性化中默认模块及各模块首页设置数据
                userMapper.deleteUserProfileByUserUuidAndModuleId(oldUuid, null);
                userMapper.updateUser(userVo);
            } else {
                userMapper.insertUser(userVo);
            }
            userMapper.insertUserPassword(userVo);
            JSONObject userAuthObj = jsonObj.getJSONObject("userAuthList");
            if (MapUtils.isNotEmpty(userAuthObj)) {
                Set<String> keySet = userAuthObj.keySet();
                for (String key : keySet) {
                    JSONArray authArray = userAuthObj.getJSONArray(key);
                    for (int j = 0; j < authArray.size(); j++) {
                        UserAuthVo authVo = new UserAuthVo();
                        authVo.setAuth(authArray.getString(j));
                        authVo.setAuthGroup(key);
                        authVo.setUserUuid(userVo.getUuid());
                        userMapper.insertUserAuth(authVo);
                    }
                }
            }
        } else {
            UserVo existUserVo = userMapper.getUserBaseInfoByUuid(uuid);
            if (existUserVo == null || Objects.equals(existUserVo.getIsDelete(), 1)) {
                throw new UserNotFoundException(uuid);
            }
            userVo.setUuid(uuid);
            userMapper.updateUser(userVo);
            // 删除用户角色
            userMapper.deleteUserRoleByUserUuid(userVo.getUuid());
            // 删除用户组
            userMapper.deleteUserTeamByUserUuid(userVo.getUuid());
            //更新密码
            if (StringUtils.isNotBlank(userVo.getPassword())) {
                userMapper.updateUserPasswordActive(userVo.getUuid());
                List<Long> idList = userMapper.getLimitUserPasswordIdList(userVo.getUuid());
                if (CollectionUtils.isNotEmpty(idList)) {
                    userMapper.deleteUserPasswordByLimit(userVo.getUuid(), idList);
                }
                userMapper.insertUserPassword(userVo);
            }
        }

        JSONArray teamUuidArray = jsonObj.getJSONArray("teamUuidList");
        if (CollectionUtils.isNotEmpty(teamUuidArray)) {
            List<String> teamUuidList = teamUuidArray.toJavaList(String.class);
            for (String teamUuid : teamUuidList) {
                userMapper.insertUserTeam(userVo.getUuid(), teamUuid.replaceAll(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
            }
        }

        JSONArray roleUuidArray = jsonObj.getJSONArray("roleUuidList");
        if (CollectionUtils.isNotEmpty(roleUuidArray)) {
            List<String> roleUuidList = roleUuidArray.toJavaList(String.class);
            for (String roleUuid : roleUuidList) {
                userMapper.insertUserRole(userVo.getUuid(), roleUuid.replaceAll(GroupSearch.ROLE.getValuePlugin(), StringUtils.EMPTY));
            }
        }

        return userVo.getUuid();
    }

    public IValid userId() {
        return value -> {
            UserVo userVo = JSON.toJavaObject(value, UserVo.class);
            if (StringUtils.isBlank(userVo.getUuid())) {
                userVo.setUuid(UuidUtil.randomUuid());
            }
            if (userMapper.checkUserIdIsIsRepeat(userVo) > 0) {
                return new FieldValidResultVo(new UserIdRepeatException(userVo.getUserId()));
            }
            return new FieldValidResultVo();
        };
    }
}
