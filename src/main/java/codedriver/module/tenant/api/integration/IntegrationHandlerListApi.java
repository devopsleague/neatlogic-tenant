package codedriver.module.tenant.api.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationHandlerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class IntegrationHandlerListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "integration/handler/list";
	}

	@Override
	public String getName() {
		return "集成信息处理组件列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({ @Param(name = "Return", explode = IntegrationHandlerVo[].class, desc = "信息处理组件列表") })
	@Description(desc = "集成信息处理组件列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return IntegrationHandlerFactory.getHandlerList();
	}
}
