package codedriver.module.tenant.api.auth;

import java.util.*;

import codedriver.framework.dto.UserDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.ModuleUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.ModuleGroupVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-06-04
 **/
@Service
public class AuthModuleGetApi extends ApiComponentBase {
	
	@Autowired
	UserMapper userMapper;
    @Override
    public String getToken() {
        return "auth/module/get";
    }

    @Override
    public String getName() {
        return "获取用户模块对应权限接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input( {
            
    })

    @Output({
            @Param( explode = ModuleGroupVo.class),
            @Param( name = "authList[].authDisplayName", desc = "权限名", type = ApiParamType.STRING),
            @Param( name = "authList[].authGroup", desc = "模块分组", type = ApiParamType.STRING),
            @Param( name = "authList[].authIntroduction", desc = "权限介绍", type = ApiParamType.STRING),
            @Param( name = "authList[].authName", desc = "权限", type = ApiParamType.STRING),
    })

    @Description(desc = "根据用户获取模块以及对应的权限列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray retrunArray = new JSONArray();
        Set<String> authGroupSet = new HashSet<String>();
        Set<String> authSet = new HashSet<String>();
        //获取用户权限
        List<UserAuthVo>  userAuthList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(UserContext.get().getUserUuid()));
        for(UserAuthVo userAuth:userAuthList) {
        	authGroupSet.add(userAuth.getAuthGroup());
        	authSet.add(userAuth.getAuth());
        }
        //****获取用户默认模块首页开始****
        HashMap<String,Map<String,String>> OuterMap = new HashMap<>(16);
        UserDataVo userDataVo = userMapper.getUserDataByUserUuidAndType(UserContext.get().getUserUuid(),"defaultModulePage");
        if(userDataVo != null) {
	        String data = userDataVo.getData();
	        JSONObject dataJson = JSONObject.parseObject(data);
	        JSONArray defaultModulePageList = dataJson.getJSONArray("defaultModulePageList");
	        for(int i = 0;i < defaultModulePageList.size();i++){
	            JSONObject o = defaultModulePageList.getJSONObject(i);
	            String group = o.getString("group");
	            String isDefault = o.getString("isDefault");
	            String defaultPage = o.getString("defaultPage");
	            Map<String,String> innerMap = new HashMap<>(2);
	            innerMap.put("isDefault",isDefault);
	            innerMap.put("defaultPage",defaultPage);
	            OuterMap.put(group,innerMap);
	        }
        }
        //****获取用户默认模块首页结束****
        Map<String, List<AuthBase>>  authModuleMap = AuthFactory.getAuthGroupMap();
        List<ModuleGroupVo> activeModuleGroupList = TenantContext.get().getActiveModuleGroupList();
        for(ModuleGroupVo moduleGroupVo : activeModuleGroupList) {
        	JSONObject moduleGroupJson = new JSONObject();
        	//把用户默认模块首页配置放入moduleGroupJson中
            Map<String,String> map = OuterMap.get(moduleGroupVo.getGroup());
        	if(map != null){
                moduleGroupJson.put("isDefault", Integer.valueOf(map.get("isDefault")));
                moduleGroupJson.put("defaultPage", map.get("defaultPage"));
            }else{
                moduleGroupJson.put("isDefault", Integer.valueOf(0));
                moduleGroupJson.put("defaultPage", "");
            }
        	moduleGroupJson.put("group", moduleGroupVo.getGroup());
        	moduleGroupJson.put("groupName", moduleGroupVo.getGroupName());
        	moduleGroupJson.put("groupSort", moduleGroupVo.getGroupSort());
        	moduleGroupJson.put("description", ModuleUtil.getModuleGroup(moduleGroupVo.getGroup()).getGroupDescription());
        	retrunArray.add(moduleGroupJson);
			/*
			 * if(!authGroupSet.contains(entry.getKey())) { continue; }
			 */
        	List<AuthBase> authBaseList = new ArrayList<AuthBase>();
        	if(authModuleMap.containsKey(moduleGroupVo.getGroup())) {
        		Iterator<AuthBase> authIterator = authModuleMap.get(moduleGroupVo.getGroup()).iterator();
	        	while(authIterator.hasNext()) {
	        		AuthBase tmpAuth = authIterator.next();
	        		if(authSet.contains(tmpAuth.getAuthName())) {
	        			authBaseList.add(tmpAuth);
	        		}
	        	}
        	}
        	moduleGroupJson.put("authList", authBaseList);
        }
    	retrunArray.sort(Comparator.comparing(obj-> ((JSONObject) obj).getInteger("groupSort")));
        return retrunArray;
    }
}
