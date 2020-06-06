package codedriver.module.tenant.api.notify;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.notify.dao.mapper.NotifyMapper;
import codedriver.framework.notify.dto.NotifyPolicyParamVo;
import codedriver.framework.notify.dto.NotifyPolicyVo;
import codedriver.framework.notify.exception.NotifyPolicyNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@IsActived
public class NotifyPolicyParamDeleteApi extends ApiComponentBase {
	
	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/policy/param/delete";
	}

	@Override
	public String getName() {
		return "通知策略参数删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "policyId", type = ApiParamType.LONG, isRequired = true, desc = "策略id"),
		@Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "参数名")
	})
	@Description(desc = "通知策略参数删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long policyId = jsonObj.getLong("policyId");
		NotifyPolicyVo notifyPolicyVo = notifyMapper.getNotifyPolicyById(policyId);
		if(notifyPolicyVo == null) {
			throw new NotifyPolicyNotFoundException(policyId.toString());
		}
		String handler = jsonObj.getString("handler");
		JSONObject config = notifyPolicyVo.getConfig();
		List<NotifyPolicyParamVo> paramList = JSON.parseArray(config.getJSONArray("paramList").toJSONString(), NotifyPolicyParamVo.class);
		Iterator<NotifyPolicyParamVo> iterator = paramList.iterator();
		while(iterator.hasNext()) {
			NotifyPolicyParamVo notifyPolicyParamVo = iterator.next();
			if(handler.equals(notifyPolicyParamVo.getHandler())) {
				iterator.remove();
			}
		}
		config.put("paramList", paramList);
		notifyPolicyVo.setConfig(config.toJSONString());
		notifyMapper.updateNotifyPolicyById(notifyPolicyVo);		
		return null;
	}

}