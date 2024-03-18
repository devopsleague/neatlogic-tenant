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

package neatlogic.module.tenant.api.globalsearch.document;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.dto.globalsearch.DocumentTypeVo;
import neatlogic.framework.fulltextindex.dto.globalsearch.DocumentVo;
import neatlogic.framework.globalsearch.core.GlobalSearchManager;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class DocumentSearchApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "globalsearch/document/search";
    }

    @Override
    public String getName() {
        return "搜索中心查询";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, isRequired = true, desc = "关键字"),
            @Param(name = "typeList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "类型列表"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "页码，默认：1")})
    @Description(desc = "搜索中心查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        DocumentVo documentVo = JSONObject.toJavaObject(jsonObj, DocumentVo.class);
        List<DocumentTypeVo> documentTypeList = GlobalSearchManager.searchDocument(documentVo);

        JSONObject returnObj = new JSONObject();
        returnObj.put("documentTypeList", documentTypeList);
        returnObj.put("wordList", documentVo.getWordList());
        return returnObj;
    }

}
