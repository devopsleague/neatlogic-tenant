package codedriver.framework.tenant.dto;

import java.io.Serializable;
import java.util.List;

import codedriver.framework.common.dto.BasePageVo;

public class RoleVo extends BasePageVo implements Serializable{

	private static final long serialVersionUID = -8007028390813552667L;

	public static final String USER_DEFAULT_ROLE = "R_SYSTEM_USER";

	private String name;
	private String description;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	

}
