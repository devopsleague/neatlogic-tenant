package codedriver.module.tenant.api.notify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@IsActived
public class NotifyPolicySystemParamListApi extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/systemparam/list";
	}

	@Override
	public String getName() {
		return "通知策略系统参数列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id")
	})
	@Output({
		@Param(name = "paramList", explode = NotifyPolicyParamVo[].class, desc = "参数列表")
	})
	@Description(desc = "通知策略参数列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
//		List<NotifyPolicyParamVo> paramList = new ArrayList<>();
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		INotifyPolicyHandler notifyPolicyHandler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyVo.getHandler());
		if(notifyPolicyHandler == null) {
			throw new NotifyPolicyHandlerNotFoundException(notifyPolicyVo.getHandler());
		}
		List<NotifyPolicyParamVo> systemParamList = notifyPolicyHandler.getSystemParamList();
//		JSONObject config = notifyPolicyVo.getConfig();
//		List<NotifyPolicyParamVo> customParamList = JSON.parseArray(config.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
//		systemParamList.addAll(customParamList);
//		String keyword = jsonObj.getString("keyword");
//		for(NotifyPolicyParamVo notifyPolicyParamVo : systemParamList) {
//			if(StringUtils.isNotBlank(keyword)) {
//				if(!notifyPolicyParamVo.getHandler().toLowerCase().contains(keyword.toLowerCase()) 
//						&& !notifyPolicyParamVo.getHandlerName().toLowerCase().contains(keyword.toLowerCase())) {
//					continue;
//				}
//			}
//			paramList.add(notifyPolicyParamVo);
//		}
		systemParamList.sort((e1, e2) -> e1.getHandler().compareToIgnoreCase(e2.getHandler()));
		JSONObject resultObj = new JSONObject();
		resultObj.put("paramList", systemParamList);
		return resultObj;
	}

}
