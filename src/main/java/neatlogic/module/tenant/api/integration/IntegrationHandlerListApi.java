/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.tenant.api.integration;

import neatlogic.framework.integration.core.IntegrationHandlerFactory;
import neatlogic.framework.integration.dto.IntegrationHandlerVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationHandlerListApi extends PrivateApiComponentBase {

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
