package codedriver.module.tenant.api.notify.job;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FRAMEWORK_BASE;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.core.INotifyContentHandler;
import codedriver.framework.notify.core.INotifyHandler;
import codedriver.framework.notify.core.NotifyContentHandlerFactory;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.exception.NotifyContentHandlerNotFoundException;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = FRAMEWORK_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class NotifyContentHandlerPreviewApi extends PrivateApiComponentBase {

	@Override
	public String getToken() {
		return "notify/content/handler/preview";
	}

	@Override
	public String getName() {
		return "获取通知内容插件预览视图";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "handler", type = ApiParamType.STRING, isRequired = true,desc = "通知内容插件"),
			@Param(name = "notifyHandler", type = ApiParamType.STRING, isRequired = true,desc = "通知插件"),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "插件可接受的额外参数，比如待我处理的工单插件，可接受dataColumnList来定制表格显示字段")
	})
	@Output({@Param(name = "content",type = ApiParamType.STRING ,desc = "预览视图HTML")})
	@Description(desc = "获取通知内容插件预览视图")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String handler = jsonObj.getString("handler");
		String notifyHandler = jsonObj.getString("notifyHandler");
		INotifyContentHandler notifyContentHandler = NotifyContentHandlerFactory.getHandler(handler);
		if(notifyContentHandler == null){
			throw new NotifyContentHandlerNotFoundException(handler);
		}
		INotifyHandler notifyHandlerObj = NotifyHandlerFactory.getHandler(notifyHandler);
		if(notifyHandlerObj == null){
			throw new NotifyHandlerNotFoundException(notifyHandler);
		}
		JSONObject result = new JSONObject();
		result.put("content",notifyContentHandler.preview(jsonObj.getJSONObject("config"),notifyHandler));
		return result;
	}
}
