/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.tenant.api.mq;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.MQ_MODIFY;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.mq.core.IMqHandler;
import neatlogic.framework.mq.core.MqHandlerFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = MQ_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListMqHandlerApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "/mq/mqhandler/list";
    }

    @Override
    public String getName() {
        return "获取消息队列类型";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "isEnable", type = ApiParamType.BOOLEAN, desc = "是否启用")})
    @Description(desc = "获取消息队列类型")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<IMqHandler> handlerList = MqHandlerFactory.getMqHandlerList();
        Boolean isEnable = jsonObj.getBoolean("isEnable");
        JSONArray returnList = new JSONArray();
        for (IMqHandler handler : handlerList) {
            if (isEnable == null) {
                returnList.add(new JSONObject() {{
                    this.put("value", handler.getName());
                    this.put("text", handler.getLabel());
                }});
            } else if (Objects.equals(isEnable, handler.isEnable())) {
                returnList.add(new JSONObject() {{
                    this.put("value", handler.getName());
                    this.put("text", handler.getLabel());
                }});
            }
        }
        return returnList;
    }

}
