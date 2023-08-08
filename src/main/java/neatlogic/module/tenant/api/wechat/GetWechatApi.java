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

package neatlogic.module.tenant.api.wechat;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NOTIFY_CONFIG_MODIFY;
import neatlogic.framework.dao.mapper.NotifyConfigMapper;
import neatlogic.framework.dto.WechatVo;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@AuthAction(action = NOTIFY_CONFIG_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetWechatApi extends PrivateApiComponentBase {

    @Resource
    private NotifyConfigMapper notifyConfigMapper;

    @Override
    public String getName() {
        return "nmtaw.getwechatapi.getname";
    }

    @Input({})
    @Output({
            @Param(explode = WechatVo.class)
    })
    @Description(desc = "nmtaw.getwechatapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String config = notifyConfigMapper.getConfigByType(NotifyHandlerType.WECHAT.getValue());
        if (StringUtils.isBlank(config)) {
            return null;
        }
        WechatVo wechatVo = JSONObject.parseObject(config, WechatVo.class);
        return wechatVo;
    }

    @Override
    public String getToken() {
        return "wechat/get";
    }
}
