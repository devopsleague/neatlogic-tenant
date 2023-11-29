/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package neatlogic.module.tenant.api.runner;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.runner.RunnerMapper;
import neatlogic.framework.dto.runner.RunnerGroupVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class RunnerGroupSearchApi extends PrivateApiComponentBase {

    @Resource
    RunnerMapper runnerMapper;

    @Override
    public String getName() {
        return "查询runner组信息";
    }

    @Override
    public String getToken() {
        return "runnergroup/search";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取runner组列表")
    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键词"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
    })
    @Output({
            @Param(name = "tbodyList", explode = RunnerGroupVo[].class, desc = "所有runner组")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        RunnerGroupVo groupVo = JSONObject.toJavaObject(paramObj, RunnerGroupVo.class);
        int rowNum = runnerMapper.searchRunnerGroupCount(groupVo);
        List<RunnerGroupVo> runnerGroupVoList = new ArrayList<>();
        groupVo.setRowNum(rowNum);
        if (rowNum > 0) {
            runnerGroupVoList = runnerMapper.searchRunnerGroup(groupVo);
        }
        return TableResultUtil.getResult(runnerGroupVoList, groupVo);
    }


}
