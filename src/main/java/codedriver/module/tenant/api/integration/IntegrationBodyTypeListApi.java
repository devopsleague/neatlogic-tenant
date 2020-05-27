package codedriver.module.tenant.api.integration;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.integration.authentication.costvalue.BodyType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@IsActived
public class IntegrationBodyTypeListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "integration/bodytype/list";
	}

	@Override
	public String getName() {
		return "集成配置请求体类型列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({ @Param(name = "value", type = ApiParamType.STRING, desc = "值"), @Param(name = "text", type = ApiParamType.STRING, desc = "显示文本") })
	@Description(desc = "集成配置请求体类型列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONArray returnList = new JSONArray();
		for (BodyType t : BodyType.values()) {
			JSONObject p = new JSONObject();
			p.put("value", t.toString());
			p.put("text", t.toString());
			returnList.add(p);
		}
		return returnList;
	}
}