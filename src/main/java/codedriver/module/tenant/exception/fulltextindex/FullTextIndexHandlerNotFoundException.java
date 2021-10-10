/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.exception.fulltextindex;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FullTextIndexHandlerNotFoundException extends ApiRuntimeException {

    public FullTextIndexHandlerNotFoundException(String type) {
        super("索引类型“" + type + "”不存在");
    }

}
