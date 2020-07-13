package codedriver.module.tenant.api.team;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class TeamUserListApi extends ApiComponentBase  {

	@Autowired
	private TeamMapper teamMapper;
	
	@Override
	public String getToken() {
		return "team/user/list";
	}

	@Override
	public String getName() {
		return "分组用户成员列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
        @Param(name = "teamUuid", type = ApiParamType.STRING, isRequired = true, desc = "分组uuid")
	})
	@Output({
		@Param(name = "teamUserList", explode = TeamUserVo[].class, desc = "分组用户成员列表")
	})
	@Description( desc = "分组用户成员列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String teamUuid = jsonObj.getString("teamUuid");
		if(teamMapper.checkTeamIsExists(teamUuid) == 0) {
			throw new TeamNotFoundException(teamUuid);
		}
		JSONObject resultObj = new JSONObject();
		List<TeamUserVo> teamUserList = teamMapper.getTeamUserListByTeamUuid(teamUuid);
		resultObj.put("teamUserList", teamUserList);
		return resultObj;
	}

}