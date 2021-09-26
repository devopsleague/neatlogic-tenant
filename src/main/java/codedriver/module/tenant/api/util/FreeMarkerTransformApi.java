/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.util;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.util.FreemarkerTransformException;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Api;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.Writer;

/**
 * @author linbq
 * @since 2021/8/26 11:55
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class FreeMarkerTransformApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "util/freeMarker/transform";
    }

    @Override
    public String getName() {
        return "freeMarker测试工具";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "freeMarkerCode", type = ApiParamType.STRING, isRequired = true, desc = "freeMarker模板"),
            @Param(name = "JSONCode", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "JSON对象")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "结果或错误信息")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String content = paramObj.getString("freeMarkerCode");
        JSONObject dataObj = paramObj.getJSONObject("JSONCode");
        try {
            if (content != null) {
                Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
                cfg.setNumberFormat("0.##");
                cfg.setClassicCompatible(true);
                StringTemplateLoader stringLoader = new StringTemplateLoader();
                stringLoader.putTemplate("template", content);
                cfg.setTemplateLoader(stringLoader);
                Template temp;
                Writer out = null;
                temp = cfg.getTemplate("template", "utf-8");
                out = new StringWriter();
                temp.process(dataObj, out);
                String resultStr = out.toString();
                out.flush();
                return resultStr;
            }
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return null;
    }
}