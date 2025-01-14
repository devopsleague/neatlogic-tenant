/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.notify;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.dto.NotifyPolicyConfigVo;
import neatlogic.framework.notify.dto.NotifyPolicyVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyTriggerConfigListApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "notify/policy/trigger/config/list";
    }

    @Override
    public String getName() {
        return "通知策略触发动作配置列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
        @Param(name = "trigger", type = ApiParamType.STRING, isRequired = true, desc = "通知触发类型")})
    @Output({@Param(name = "notifyList", type = ApiParamType.JSONARRAY, desc = "通知触发配置列表")})
    @Description(desc = "通知策略触发动作配置列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long policyId = jsonObj.getLong("policyId");
        NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
        if (notifyPolicyVo == null) {
            throw new NotifyPolicyNotFoundException(policyId.toString());
        }
        INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
        }
        List<NotifyTriggerVo> notifyTriggerList = notifyPolicyHandler.getNotifyTriggerList();
        String trigger = jsonObj.getString("trigger");
        NotifyTriggerVo notifyTriggerVo = null;
        for (NotifyTriggerVo triggerVo : notifyTriggerList) {
            if (Objects.equals(triggerVo.getTrigger(), trigger)) {
                notifyTriggerVo = triggerVo;
                break;
            }
        }
        if (notifyTriggerVo == null) {
            throw new ParamIrregularException("trigger");
        }
        JSONObject resultObj = new JSONObject();
        JSONObject authorityConfig = notifyPolicyHandler.getAuthorityConfig();
        JSONArray includeArray = authorityConfig.getJSONArray("includeList");
        JSONArray excludeArray = authorityConfig.getJSONArray("excludeList");
        List<String> includeList = notifyTriggerVo.getIncludeList();
        if (CollectionUtils.isNotEmpty(includeList)) {
            for (String include : includeList) {
                if (!includeArray.contains(include)) {
                    includeArray.add(include);
                }
                excludeArray.remove(include);
            }
        }
        List<String> excludeList = notifyTriggerVo.getExcludeList();
        if (CollectionUtils.isNotEmpty(excludeList)) {
            for (String exclude : excludeList) {
                if (!excludeArray.contains(exclude)) {
                    excludeArray.add(exclude);
                }
                includeArray.remove(exclude);
            }
        }
        resultObj.put("authorityConfig", authorityConfig);
        resultObj.put("notifyList", new JSONArray());
        NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
        List<NotifyTriggerVo> triggerList = config.getTriggerList();
        for (NotifyTriggerVo triggerObj : triggerList) {
            if (trigger.equals(triggerObj.getTrigger())) {
                resultObj.put("notifyList", triggerObj.getNotifyList());
            }
        }
        return resultObj;
    }

}
