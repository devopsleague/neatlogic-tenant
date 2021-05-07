/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.form;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.form.constvalue.IFormHandlerType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class FormHandlerListApi extends PrivateApiComponentBase {
    /**
     * 标记是否未初始化数据，只初始化一次
     **/
    private static volatile boolean isUninitialized = true;
    private static JSONArray handlerList = new JSONArray();

    @Override
    public String getToken() {
        return "form/handler/list";
    }

    @Override
    public String getName() {
        return "获取表单组件列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(name = "handler", type = ApiParamType.STRING, desc = "处理器"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "处理器中文名"),
            @Param(name = "icon", type = ApiParamType.STRING, desc = "图标"),
            @Param(name = "type", type = ApiParamType.ENUM, desc = "分类，form（表单组件）|control（控制组件）")})
    @Description(desc = "获取表单组件列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return handlerList;
    }

    static {
        if (isUninitialized) {
            synchronized (IFormHandlerType.class) {
                if (isUninitialized) {
                    Reflections reflections = new Reflections("codedriver");
                    Set<Class<? extends IFormHandlerType>> classSet =
                            reflections.getSubTypesOf(IFormHandlerType.class);
                    for (Class<? extends IFormHandlerType> c : classSet) {
                        try {
                            for (IFormHandlerType type : c.getEnumConstants()) {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("handler", type.getHandler());
                                jsonObj.put("name", type.getHandlerName());
                                jsonObj.put("icon", type.getIcon());
                                jsonObj.put("type", type.getType());
                                jsonObj.put("isConditionable", type.isConditionable());
                                jsonObj.put("isExtendable", type.isExtendable());
                                jsonObj.put("isFilterable", type.isFilterable());
                                jsonObj.put("isShowable", type.isShowable());
                                jsonObj.put("isValueable", type.isValueable());
                                jsonObj.put("module", type.getModule());
                                handlerList.add(jsonObj);
                            }
                        } catch (Exception e) {

                        }
                    }
                    isUninitialized = false;
                }
            }
        }
    }

}
