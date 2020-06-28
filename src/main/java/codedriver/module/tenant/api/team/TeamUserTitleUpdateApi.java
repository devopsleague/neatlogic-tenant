package codedriver.module.tenant.api.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.TeamUserTitle;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamUserVo;
import codedriver.framework.exception.team.TeamNotFoundException;
import codedriver.framework.exception.team.TeamUserTitleNotFoundException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class TeamUserTitleUpdateApi extends ApiComponentBase {

	@Autowired
	private TeamMapper teamMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "team/user/title/update";
	}

	@Override
	public String getName() {
		return "组用户头衔更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "teamUuid", type = ApiParamType.STRING, isRequired = true, desc = "组uuid"),
		@Param(name = "userUuid", type = ApiParamType.STRING, isRequired = true, desc = "用户uuid"),
		@Param(name = "title", type = ApiParamType.STRING, isRequired = true, desc = "头衔")
	})
	@Output({
		@Param(name="Return", explode = TeamUserVo.class, desc = "组用户头衔信息")
	})
	@Description(desc = "组用户头衔更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		TeamUserVo teamUserVo = JSON.toJavaObject(jsonObj, TeamUserVo.class);
		if(teamMapper.checkTeamIsExists(teamUserVo.getTeamUuid()) == 0) {
			throw new TeamNotFoundException(teamUserVo.getTeamUuid());
		}
		if(userMapper.checkUserIsExists(teamUserVo.getUserUuid()) == 0) {
			throw new UserNotFoundException(teamUserVo.getUserUuid());
		}
		if(TeamUserTitle.getValue(teamUserVo.getTitle()) == null) {
			throw new TeamUserTitleNotFoundException(teamUserVo.getTitle());
		}
		teamMapper.updateTeamUserTitle(teamUserVo);
		return teamUserVo;
	}

}
