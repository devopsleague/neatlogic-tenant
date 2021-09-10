/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.integration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.integration.ParamFormatInvalidException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.javascript.JavascriptUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class IntegrationTransformTestApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "integration/transformtest";
    }

    @Override
    public String getName() {
        return "集成设置参数转换测试接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "content", type = ApiParamType.STRING, desc = "原始内容，需要符合json格式", isRequired = true), @Param(name = "template", type = ApiParamType.STRING, desc = "转换模板，如果为空则不做转换")})
    @Output({@Param(name = "Return", type = ApiParamType.STRING, desc = "返回结果")})
    @Description(desc = "集成设置参数转换测试接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        String content = jsonObj.getString("content");
        String template = jsonObj.getString("template");
        Object object = null;
        try {
            object = JSONObject.parseObject(content);
        } catch (Exception ex) {
            try {
                object = JSONArray.parseArray(content);
            } catch (Exception ignored) {

            }
        }
        if (object == null) {
            throw new ParamFormatInvalidException();
        }
        JSONObject returnObj = new JSONObject();
        String returnStr;
        try {
            StringWriter sw = new StringWriter();
            returnStr = JavascriptUtil.transform(object, template, sw);
            returnObj.put("result", returnStr);
            returnObj.put("output", sw.toString());
        } catch (ApiRuntimeException e) {
            returnObj.put("error", e.getMessage(true));
        } catch (Exception e) {
            returnObj.put("error", e.getMessage());
        }

        return returnObj;
    }
}
