package codedriver.framework.tenant.service;

import java.util.List;

import codedriver.framework.tenant.dto.TeamVo;

public interface TeamService {
	
	public List<TeamVo> selectRoleTeamList(TeamVo teamVo);
	public List<TeamVo> selectTeamList(TeamVo teamVo);

}
