package codedriver.framework.tenant.api.scheduler;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.AuthAction;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.core.FrameworkExceptionMessageBase;
import codedriver.framework.exception.core.IApiExceptionMessage;
import codedriver.framework.exception.type.CustomExceptionMessage;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Example;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobClassVo;
import codedriver.framework.scheduler.exception.SchedulerExceptionMessage;
@Service
@AuthAction(name="SYSTEM_JOB_EDIT")
public class JobClassGetApi extends ApiComponentBase {
	
	Logger logger = LoggerFactory.getLogger(JobClassGetApi.class);
	
	@Autowired
	private SchedulerMapper schedulerMapper;
	
	@Override
	public String getToken() {
		return "job/class/get";
	}

	@Override
	public String getName() {
		return "获取定时作业组件信息";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({@Param(name="classpath",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件classpath")})
	@Description(desc="获取定时作业组件信息")
	@Example(example="{\"classpath\":\"codedriver.framework.scheduler.core.TestJob\"}")
	@Output({
		@Param(name="name",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件名称"),
		@Param(name="classpath",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件classpath"),
		@Param(name="moduleName",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件所属模块名"),
		@Param(name="type",type=ApiParamType.STRING,isRequired=true,desc="定时作业组件级别类型"),
		@Param(name="inputList",type=ApiParamType.JSONARRAY,isRequired=true,desc="属性列表"),
		@Param(name="inputList[0].name",type=ApiParamType.STRING,isRequired=true,desc="属性名称"),
		@Param(name="inputList[0].dataType",type=ApiParamType.STRING,isRequired=true,desc="属性数据类型"),
		@Param(name="inputList[0].description",type=ApiParamType.STRING,isRequired=true,desc="属性说明"),
		@Param(name="inputList[0].required",type=ApiParamType.BOOLEAN,isRequired=true,desc="属性是否必填")		
		})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JobClassVo jobClassVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<JobClassVo>() {});
		TenantContext tenant = TenantContext.get();
		tenant.setUseDefaultDatasource(false);
		String tenantUuid = tenant.getTenantUuid();
		jobClassVo.setTenantUuid(tenantUuid);
		tenant.setUseDefaultDatasource(true);
		JobClassVo jobClass = schedulerMapper.getJobClassByClasspath(jobClassVo);
		if(jobClass == null) {
			IApiExceptionMessage message = new FrameworkExceptionMessageBase(new SchedulerExceptionMessage(new CustomExceptionMessage("定时作业组件："+ jobClassVo.getClasspath() + " 不存在")));
			logger.error(message.toString());
			throw new ApiRuntimeException(message);
		}
		JSONArray inputList = new JSONArray();
		IJob job = SchedulerManager.getInstance(jobClass.getClasspath());
		Map<String, codedriver.framework.scheduler.annotation.Param> paramMap = job.initProp();
		for(Entry<String, codedriver.framework.scheduler.annotation.Param> entry : paramMap.entrySet()) {
			codedriver.framework.scheduler.annotation.Param param = entry.getValue();
			JSONObject paramObj = new JSONObject();
            paramObj.put("name", param.name());
            paramObj.put("dataType", param.dataType());
            paramObj.put("description", param.description());
            paramObj.put("required", param.required());
            inputList.add(paramObj);
		}
		JSONObject returnObj = new JSONObject();
		returnObj.put("jobClass", jobClass);
		returnObj.put("inputList", inputList);
		return returnObj;
	}

}
