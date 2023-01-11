/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */
package codedriver.module.tenant.api.scheduler;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.SCHEDULE_JOB_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author longrf
 * @date 2023/1/10 11:40
 */

@Service
@AuthAction(action = SCHEDULE_JOB_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchSchedulerNameMemoryApi extends PrivateApiComponentBase {

    @Resource
    private SchedulerFactoryBean schedulerFactoryBean;

    @Override
    public String getName() {
        return "获取定时作业名称列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "jobGroupName", type = ApiParamType.STRING, isRequired = true, desc = "作业组名"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字")
    })
    @Output({
            @Param(explode = ValueTextVo[].class, desc = "定时作业名称列表")
    })
    @Description(desc = "获取定时作业名称列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String jobGroupName = paramObj.getString("jobGroupName");
        String keyword = paramObj.getString("keyword");
        List<ValueTextVo> returnList = new ArrayList<>();
        String tenantString = TenantContext.get().getTenantUuid();
        int length = tenantString.length();
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName))) {
            if (jobKey.getName() != null) {
                if (StringUtils.isNotEmpty(keyword)) {
                    if (jobKey.getName().contains(keyword)) {
                        returnList.add(new ValueTextVo(jobKey.getName(), jobKey.getName().startsWith(tenantString) ? jobKey.getName().substring(length + 1) : jobKey.getName()));
                    }
                } else {
                    returnList.add(new ValueTextVo(jobKey.getName(), jobKey.getName().startsWith(tenantString) ? jobKey.getName().substring(length + 1) : jobKey.getName()));
                }
                break;
            }
        }
        return returnList;
    }

    @Override
    public String getToken() {
        return "scheduler/name/search";
    }
}
