package codedriver.module.tenant.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class SCHEDULE_JOB_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "定时作业管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对定时作业进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}

	@Override
	public Integer sort() {
		return 16;
	}
}
