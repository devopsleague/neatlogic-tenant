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

package neatlogic.module.tenant.api.form;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListFormTagApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/tag/list";
    }

    @Override
    public String getName() {
        return "nmtaf.listformtagapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "formUuid", type = ApiParamType.STRING, isRequired = true, desc = "term.framework.formuuid")
    })
    @Output({
            @Param(name = "tbodyList", explode = String[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtaf.listformtagapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String formUuid = jsonObj.getString("formUuid");
        FormVo form = formMapper.getFormByUuid(formUuid);
        if (form == null) {
            throw new FormNotFoundException(formUuid);
        }
        FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(formUuid);
        if (formVersionVo == null) {
            throw new FormActiveVersionNotFoundExcepiton(form.getName());
        }
        Set<String> tagSet = new HashSet<>();
//        List<FormAttributeVo> formExtendAttributeList = formVersionVo.getFormExtendAttributeList();
//        if (CollectionUtils.isNotEmpty(formExtendAttributeList)) {
//            for (FormAttributeVo formAttributeVo : formExtendAttributeList) {
//                tagSet.add(formAttributeVo.getTag());
//            }
//        }
        List<FormAttributeVo> formCustomExtendAttributeList = formVersionVo.getFormCustomExtendAttributeList();
        if (CollectionUtils.isNotEmpty(formCustomExtendAttributeList)) {
            for (FormAttributeVo formAttributeVo : formCustomExtendAttributeList) {
                tagSet.add(formAttributeVo.getTag());
            }
        }

        return TableResultUtil.getResult(new ArrayList<>(tagSet));
    }

}
