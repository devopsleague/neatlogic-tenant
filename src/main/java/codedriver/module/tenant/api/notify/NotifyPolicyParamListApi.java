package codedriver.module.tenant.api.notify;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyConfigVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyPolicyParamListApi extends PrivateApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/param/list";
	}

	@Override
	public String getName() {
		return "通知策略参数列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "模糊匹配"),
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id")
	})
	@Output({
		@Param(name = "paramList", explode = ConditionParamVo[].class, desc = "参数列表")
	})
	@Description(desc = "通知策略参数列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ConditionParamVo> paramList = new ArrayList<>();
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
		List<ConditionParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
		NotifyPolicyConfigVo config = notifyPolicyVo.getConfig();
		List<ConditionParamVo> customParamList = config.getParamList();
		systemParamList.addAll(customParamList);
		String keyword = jsonObj.getString("keyword");
		for(ConditionParamVo notifyPolicyParamVo : systemParamList) {
			if(StringUtils.isNotBlank(keyword)) {
				if(!notifyPolicyParamVo.getName().toLowerCase().contains(keyword.toLowerCase()) 
						&& !notifyPolicyParamVo.getLabel().toLowerCase().contains(keyword.toLowerCase())) {
					continue;
				}
			}
			paramList.add(notifyPolicyParamVo);
		}
		paramList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
		JSONObject resultObj = new JSONObject();
		resultObj.put("paramList", paramList);
		return resultObj;
	}

}
