package codedriver.framework.tenant.api.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.framework.scheduler.service.SchedulerService;
@Service
@Transactional
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobAuditSearchApi extends ApiComponentBase {

	@Autowired
	private SchedulerService schedulerService;
	
	@Override
	public String getToken() {
		return "job/audit/search";
	}

	@Override
	public String getName() {
		return "查询定时作业执行记录列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=false,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=false,desc="页大小"),
		@Param(name="jobUuid",type=ApiParamType.STRING,isRequired=true,desc="定时作业uuid")
		})
	@Description(desc="查询定时作业执行记录列表")
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="jobAuditList",explode=JobAuditVo[].class,desc="执行记录列表")
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobAuditVo jobAuditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobAuditVo>() {});		
		List<JobAuditVo> jobAuditList = schedulerService.searchJobAudit(jobAuditVo);
		JSONObject resultObj = new JSONObject();
		resultObj.put("jobAuditList", jobAuditList);
		resultObj.put("currentPage",jobAuditVo.getCurrentPage());
		resultObj.put("pageSize",jobAuditVo.getPageSize());
		resultObj.put("pageCount", jobAuditVo.getPageCount());
		resultObj.put("rowNum", jobAuditVo.getRowNum());
		return resultObj;
	}

}
