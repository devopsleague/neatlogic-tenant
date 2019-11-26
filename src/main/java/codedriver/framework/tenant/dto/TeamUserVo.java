package codedriver.framework.tenant.dto;

import java.util.List;

import codedriver.framework.common.dto.BasePageVo;



public class TeamUserVo  extends BasePageVo{
	public static int TYPE_USER;
	public static int TYPE_MANAGER;
	
	private String teamUuid;
	private String userId;
	private String userName;
	private String teamName;
	private int type;
	List<Long> teamIdList ; 
	
	public TeamUserVo() {
	}
	public TeamUserVo(List<Long> teamIdList) {
		this.teamIdList = teamIdList ; 
	}

	public String getTeamUuid() {
		return teamUuid;
	}
	public void setTeamUuid(String teamUuid) {
		this.teamUuid = teamUuid;
	}
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<Long> getTeamIdList() {
		return teamIdList;
	}

	public void setTeamIdList(List<Long> teamIdList) {
		this.teamIdList = teamIdList;
	}
	
}
