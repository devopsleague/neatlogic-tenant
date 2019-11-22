package codedriver.framework.tenant.service;

import java.util.List;

import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.scheduler.dto.JobVo;

public interface SchedulerService {

	List<JobVo> searchJobList(JobVo jobVo);

	List<JobClassVo> searchJobClassList(JobClassVo jobClassVo);

	List<JobAuditVo> searchJobAuditList(JobAuditVo jobAuditVo);

	int saveJob(JobVo job);

	int saveJobClass(JobClassVo jobClassVo);

}
