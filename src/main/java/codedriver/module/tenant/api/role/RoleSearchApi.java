package codedriver.module.tenant.api.role;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.service.RoleService;

@Service
public class RoleSearchApi extends ApiComponentBase {

	@Autowired
	private RoleService roleService;

	@Override
	public String getToken() {
		return "role/search";
	}

	@Override
	public String getName() {
		return "角色查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword",
					type = ApiParamType.STRING,
					desc = "关键字，匹配名称或说明", xss = true),
			@Param(name = "authGroup",
					type = ApiParamType.STRING,
					desc = "权限模块"),
			@Param(name = "auth",
					type = ApiParamType.STRING,
					desc = "权限"),
			@Param(name = "needPage",
					type = ApiParamType.BOOLEAN,
					desc = "是否需要分页，默认true"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页条目"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页") })
	@Output({
			@Param(name = "tbodyList",
					explode = RoleVo[].class,
					desc = "table数据列表"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页数据条目"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页"),
			@Param(name = "rowNum",
					type = ApiParamType.INTEGER,
					desc = "返回条目总数")})
	@Description(desc = "角色查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject returnObj = new JSONObject();
		RoleVo roleVo = new RoleVo();
		roleVo.setKeyword(jsonObj.getString("keyword"));
		if (jsonObj.containsKey("currentPage")){
			roleVo.setCurrentPage(jsonObj.getInteger("currentPage"));
		}
		if (jsonObj.containsKey("auth")){
			roleVo.setAuth(jsonObj.getString("auth"));
		}
		if(jsonObj.containsKey("authModule")){
			roleVo.setAuthGroup(jsonObj.getString("authGroup"));
		}
		if (jsonObj.containsKey("needPage")){
			roleVo.setNeedPage(jsonObj.getBoolean("needPage"));
		}
		List<RoleVo> roleList = roleService.searchRole(roleVo);
		if (roleVo.getNeedPage()) {
			returnObj.put("pageSize", roleVo.getPageSize());
			returnObj.put("currentPage", roleVo.getCurrentPage());
			returnObj.put("rowNum", roleVo.getRowNum());
		}
		returnObj.put("tbodyList", roleList);
		return returnObj;
	}
}
