package codedriver.framework.tenant.api.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.AuthAction;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.tenant.service.TeamService;

@AuthAction(name="SYSTEM_TEAM_EDIT")
@Service
public class TeamGetApi extends ApiComponentBase{

	@Autowired
	private TeamService teamService;
	
	@Override
	public String getToken() {
		return "team/get";
	}

	@Override
	public String getName() {
		return "获取组信息接口";
	}
	
	@Override
	public String getConfig() {
		return null;
	}


	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "组id",isRequired=true)})
	@Output({@Param(name = "uuid", type = ApiParamType.STRING, desc = "组id"),
		@Param(name = "name", type = ApiParamType.STRING, desc = "组名"),
		@Param(name = "description", type = ApiParamType.STRING, desc = "组描述"),
		@Param(name = "isHandletask", type = ApiParamType.STRING, desc = "是否允许处理下级任务")})
	@Description(desc = "获取组信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject json = new JSONObject();
		String teamUuid = jsonObj.getString("uuid");
		TeamVo teamVo = teamService.getTeamByUuid(teamUuid);
		if(teamVo!=null) {
			json.put("uuid", teamVo.getUuid());
			json.put("name", teamVo.getName());
			json.put("description", teamVo.getDescription());
			json.put("isHandletask", teamVo.getIsHandleChildtask());
		}
		return json;
	}
}
