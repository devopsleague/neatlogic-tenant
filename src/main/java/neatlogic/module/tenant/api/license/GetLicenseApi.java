/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.api.license;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.ADMIN;
import neatlogic.framework.auth.label.LICENSE_MODIFY;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.dto.LicenseVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.LicenseUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = ADMIN.class)
@AuthAction(action = LICENSE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetLicenseApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "license/get";
    }

    @Override
    public String getName() {
        return "nmtal.getlicenseapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = LicenseVo.class)})
    @Description(desc = "nmtal.getlicenseapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String licensePk = Config.LICENSE_PK();
        String license = Config.LICENSE();
        if (StringUtils.isNotBlank(license) && StringUtils.isNotBlank(licensePk)) {
            return LicenseUtil.deLicense(license, licensePk);
        }
        return null;
    }
}
