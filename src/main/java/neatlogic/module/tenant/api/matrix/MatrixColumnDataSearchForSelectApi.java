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

package neatlogic.module.tenant.api.matrix;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.CacheControlType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.matrix.constvalue.SearchExpression;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.core.MatrixPrivateDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.*;
import neatlogic.framework.matrix.exception.MatrixAttributeNotFoundException;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service

@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForSelectApi extends PrivateApiComponentBase {

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/search/forselect";
    }

    @Override
    public String getName() {
        return "矩阵属性数据查询-下拉接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    @CacheControl(cacheControlType = CacheControlType.MAXAGE, maxAge = 30000)
    @Input({
            @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING, xss = true),
            @Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING),
            @Param(name = "keywordColumn", desc = "关键字属性uuid", type = ApiParamType.STRING),
            @Param(name = "valueField", desc = "value属性uuid", type = ApiParamType.STRING),
            @Param(name = "textField", desc = "text属性uuid", type = ApiParamType.STRING),
            @Param(name = "hiddenFieldList", desc = "隐藏属性uuid列表", type = ApiParamType.JSONARRAY),
            @Param(name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER),
            @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
            @Param(name = "defaultValue", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY),
            @Param(name = "filterList", desc = "过滤条件集合", type = ApiParamType.JSONARRAY),

            @Param(name = "matrixLabel", desc = "矩阵名", type = ApiParamType.STRING),
            @Param(name = "keywordColumnUniqueIdentifier", desc = "关键字属性唯一标识", type = ApiParamType.STRING),
            @Param(name = "valueFieldUniqueIdentifier", desc = "value属性唯一标识", type = ApiParamType.STRING),
            @Param(name = "textFieldUniqueIdentifier", desc = "text属性唯一标识", type = ApiParamType.STRING),
            @Param(name = "hiddenFieldUniqueIdentifierList", desc = "隐藏属性唯一标识列表", type = ApiParamType.JSONARRAY)
    })
    @Output({
            @Param(name = "dataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Example(example = "" +
            "{" +
            "\"matrixUuid(矩阵uuid，必填)\": \"825e6ba09050406eb0de8c4bdcd4e27c\"," +
            "\"columnList(需要返回数据的字段列表，必填)\": [" +
            "\"92196814d8da4ad9bed63e1d650d7e98\"," +
            "\"a4e9978fd06d46d78b13f947a2b1b188\"," +
            "\"cf2c2677c18540a79e60cfd9d531b50c\"," +
            "\"b5d685e9e5fb4ce0baa0604de812e93b\"," +
            "\"6e4abb9b532b49139cec798a3828c7cd\"," +
            "\"3d2f5475138744938f0bb1da1f82002c\"," +
            "\"96d449a58b664a31b32bb1c28090aeee\"" +
            "]," +
            "\"searchColumnList(可搜索字段列表，选填)\": [" +
            "\"92196814d8da4ad9bed63e1d650d7e98\"," +
            "\"a4e9978fd06d46d78b13f947a2b1b188\"" +
            "\t]," +
            "\"currentPage\": 1," +
            "\"pageSize\": 10," +
            "\"sourceColumnList(过滤条件列表，选填)\": [" +
            "{" +
            "\"column(字段uuid，必填)\": \"a4e9978fd06d46d78b13f947a2b1b188\"," +
            "\"expression(过滤表达式，必填)\": \"like|notlike|equal|unequal|include|exclude|between|greater-than|less-than|is-null|match|is-not-null\"," +
            "\"valueList(过滤值列表，必填)\": [" +
            "\"1\"" +
            "]" +
            "}" +
            "]," +
            "\"filterList(联动过滤条件列表，选填)\": [" +
            "{" +
            "\"uuid(字段uuid，必填)\": \"92196814d8da4ad9bed63e1d650d7e98\"," +
            "\"valueList(过滤值列表，必填)\": [" +
            "\"2\"" +
            "]" +
            "}" +
            "]" +
            "}")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        jsonObj.remove("needPage");
        MatrixDataVo dataVo = jsonObj.toJavaObject(MatrixDataVo.class);
        if (StringUtils.isBlank(dataVo.getMatrixUuid()) && StringUtils.isBlank(dataVo.getMatrixLabel())) {
            throw new ParamNotExistsException("matrixUuid", "matrixLabel");
        }
        MatrixVo matrixVo = null;
        if (StringUtils.isNotBlank(dataVo.getMatrixUuid())) {
            matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVo(dataVo.getMatrixUuid());
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
                if (matrixVo == null) {
                    throw new MatrixNotFoundException(dataVo.getMatrixUuid());
                }
            }
        } else if (StringUtils.isNotBlank(dataVo.getMatrixLabel())) {
            matrixVo = MatrixPrivateDataSourceHandlerFactory.getMatrixVoByLabel(dataVo.getMatrixLabel());
            if (matrixVo == null) {
                matrixVo = matrixMapper.getMatrixByLabel(dataVo.getMatrixLabel());
                if (matrixVo == null) {
                    throw new MatrixNotFoundException(dataVo.getMatrixLabel());
                }
            }
            dataVo.setMatrixUuid(matrixVo.getUuid());
        }

        IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
        if (matrixDataSourceHandler == null) {
            throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
        }

        List<MatrixAttributeVo> matrixAttributeList = matrixDataSourceHandler.getAttributeList(matrixVo);
        if (CollectionUtils.isEmpty(matrixAttributeList)) {
            return new JSONObject();
        }
        Map<String, String> uniqueIdentifierToUuidMap = new HashMap<>();
        for (MatrixAttributeVo matrixAttributeVo : matrixAttributeList) {
            String uniqueIdentifier = matrixAttributeVo.getUniqueIdentifier();
            if (StringUtils.isBlank(uniqueIdentifier)) {
                continue;
            }
            uniqueIdentifierToUuidMap.put(uniqueIdentifier, matrixAttributeVo.getUuid());
        }
        List<MatrixFilterVo> filterList = dataVo.getFilterList();
        if (CollectionUtils.isNotEmpty(filterList)) {
            Iterator<MatrixFilterVo> iterator = filterList.iterator();
            while (iterator.hasNext()) {
                MatrixFilterVo matrixFilterVo = iterator.next();
                if (StringUtils.isBlank(matrixFilterVo.getUuid())) {
                    if (StringUtils.isBlank(matrixFilterVo.getUniqueIdentifier())) {
                        iterator.remove();
                        continue;
                    }
                    String attrUuid = uniqueIdentifierToUuidMap.get(matrixFilterVo.getUniqueIdentifier());
                    if (StringUtils.isBlank(attrUuid)) {
                        iterator.remove();
                        continue;
                    }
                    matrixFilterVo.setUuid(attrUuid);
                }
                if (CollectionUtils.isEmpty(matrixFilterVo.getValueList())
                        && !Objects.equals(matrixFilterVo.getExpression(), SearchExpression.NULL.getExpression())
                        && !Objects.equals(matrixFilterVo.getExpression(), SearchExpression.NOTNULL.getExpression())
                ) {
                    iterator.remove();
                }
            }
        }
        Set<String> notNullColumnSet = new HashSet<>();
        List<String> attributeList = matrixAttributeList.stream().map(MatrixAttributeVo::getUuid).collect(Collectors.toList());
        String valueField = jsonObj.getString("valueField");
        String valueFieldUniqueIdentifier = jsonObj.getString("valueFieldUniqueIdentifier");
        if (StringUtils.isNotBlank(valueField)) {
            if (!attributeList.contains(valueField)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), valueField);
            }
        } else {
            if (StringUtils.isBlank(valueFieldUniqueIdentifier)) {
                throw new ParamNotExistsException("valueField", "valueFieldUniqueIdentifier");
            }
            String attrUuid = uniqueIdentifierToUuidMap.get(valueFieldUniqueIdentifier);
            if (StringUtils.isBlank(attrUuid)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), valueFieldUniqueIdentifier);
            }
            valueField = attrUuid;
        }
        notNullColumnSet.add(valueField);
        String textField = jsonObj.getString("textField");
        String textFieldUniqueIdentifier = jsonObj.getString("textFieldUniqueIdentifier");
        if (StringUtils.isNotBlank(textField)) {
            if (!attributeList.contains(textField)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), textField);
            }
        } else {
            if (StringUtils.isBlank(textFieldUniqueIdentifier)) {
                throw new ParamNotExistsException("textField", "textFieldUniqueIdentifier");
            }
            String attrUuid = uniqueIdentifierToUuidMap.get(textFieldUniqueIdentifier);
            if (StringUtils.isBlank(attrUuid)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), textFieldUniqueIdentifier);
            }
            textField = attrUuid;
        }
        List<Map<String, JSONObject>> resultList = new ArrayList<>();
        dataVo.setKeywordColumn(textField);
        notNullColumnSet.add(textField);
        List<String> columnList = new ArrayList<>();
        columnList.add(valueField);
        columnList.add(textField);
        JSONArray hiddenFieldList = jsonObj.getJSONArray("hiddenFieldList");
        JSONArray hiddenFieldUniqueIdentifierList = jsonObj.getJSONArray("hiddenFieldUniqueIdentifierList");
        if (CollectionUtils.isNotEmpty(hiddenFieldList)) {
            for (int i = 0; i < hiddenFieldList.size(); i++) {
                String hiddenField = hiddenFieldList.getString(i);
                if (StringUtils.isNotBlank(hiddenField)) {
                    if (!attributeList.contains(hiddenField)) {
                        throw new MatrixAttributeNotFoundException(matrixVo.getName(), hiddenField);
                    }
                    columnList.add(hiddenField);
                }
            }
        } else if (CollectionUtils.isNotEmpty(hiddenFieldUniqueIdentifierList)) {
            for (int i = 0; i < hiddenFieldUniqueIdentifierList.size(); i++) {
                String hiddenFieldUniqueIdentifier = hiddenFieldUniqueIdentifierList.getString(i);
                if (StringUtils.isBlank(hiddenFieldUniqueIdentifier)) {
                    continue;
                }
                String hiddenField = uniqueIdentifierToUuidMap.get(hiddenFieldUniqueIdentifier);
                if (StringUtils.isBlank(hiddenField)) {
                    throw new MatrixAttributeNotFoundException(matrixVo.getName(), hiddenFieldUniqueIdentifier);
                }
                columnList.add(hiddenField);
            }
        }
        dataVo.setColumnList(columnList);
        dataVo.setNotNullColumnList(new ArrayList<>(notNullColumnSet));

        String keywordColumn = dataVo.getKeywordColumn();
        String keywordColumnUniqueIdentifier = dataVo.getKeywordColumnUniqueIdentifier();
        if (StringUtils.isBlank(keywordColumn) && StringUtils.isNotBlank(keywordColumnUniqueIdentifier)) {
            String attrUuid = uniqueIdentifierToUuidMap.get(keywordColumnUniqueIdentifier);
            if (StringUtils.isBlank(attrUuid)) {
                throw new MatrixAttributeNotFoundException(matrixVo.getName(), keywordColumnUniqueIdentifier);
            }
            dataVo.setKeywordColumn(attrUuid);
        }
        JSONArray defaultValue = dataVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<MatrixDefaultValueFilterVo> defaultValueFilterList = new ArrayList<>();
            for (Object defaultValueObject : defaultValue) {
                if (defaultValueObject instanceof JSONObject) {
                    JSONObject defaultValueObj = (JSONObject) defaultValueObject;
                    String value = defaultValueObj.getString("value");
                    String text = defaultValueObj.getString("text");
                    MatrixDefaultValueFilterVo matrixDefaultValueFilterVo = new MatrixDefaultValueFilterVo(
                            new MatrixKeywordFilterVo(valueField, SearchExpression.EQ.getExpression(), value),
                            new MatrixKeywordFilterVo(textField, SearchExpression.EQ.getExpression(), text)
                    );
                    defaultValueFilterList.add(matrixDefaultValueFilterVo);
                } else if (defaultValueObject instanceof String) {
                    String defaultValueStr = (String) defaultValueObject;
                    MatrixDefaultValueFilterVo matrixDefaultValueFilterVo = new MatrixDefaultValueFilterVo(
                            new MatrixKeywordFilterVo(valueField, SearchExpression.EQ.getExpression(), defaultValueStr),
                            null
                    );
                    defaultValueFilterList.add(matrixDefaultValueFilterVo);
                }
            }
            dataVo.setDefaultValueFilterList(defaultValueFilterList);
            dataVo.setDefaultValue(null);

            resultList = matrixDataSourceHandler.searchTableDataNew(dataVo);
            deduplicateData(null, valueField, textField, resultList);
        } else {
            List<Map<String, JSONObject>> previousPageList = new ArrayList<>();
            int startNum = dataVo.getStartNum();
            int currentPageBackup = dataVo.getCurrentPage();
            int pageSize = dataVo.getPageSize();
            int currentPage = 0;
            while (resultList.size() < pageSize) {
                currentPage++;
                dataVo.setCurrentPage(currentPage);
                List<Map<String, JSONObject>> list = matrixDataSourceHandler.searchTableDataNew(dataVo);
                deduplicateData(previousPageList, valueField, textField, list);
                for (Map<String, JSONObject> element : list) {
                    previousPageList.add(element);
                    if (previousPageList.size() > startNum) {
                        resultList.add(element);
                        if (resultList.size() >= pageSize) {
                            break;
                        }
                    }
                }
                if (currentPage >= dataVo.getPageCount()) {
                    break;
                }
            }
            dataVo.setCurrentPage(currentPageBackup);
        }
        JSONArray dataList = new JSONArray();
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (Map<String, JSONObject> result : resultList) {
                JSONObject element = new JSONObject();
                JSONObject valueObj = result.get(valueField);
                if (MapUtils.isNotEmpty(valueObj)) {
                    String valueStr = valueObj.getString("value");
                    element.put("value", valueStr);
                }
                JSONObject textObj = result.get(textField);
                if (MapUtils.isNotEmpty(textObj)) {
                    String textStr = textObj.getString("text");
                    element.put("text", textStr);
                }
                if (CollectionUtils.isNotEmpty(hiddenFieldList)) {
                    for (int i = 0; i < hiddenFieldList.size(); i++) {
                        String hiddenField = hiddenFieldList.getString(i);
                        if (StringUtils.isBlank(hiddenField)) {
                            continue;
                        }
//                        if (Objects.equals(hiddenField, valueField)) {
//                            continue;
//                        }
                        JSONObject hiddenFieldObj = result.get(hiddenField);
                        if (MapUtils.isNotEmpty(hiddenFieldObj)) {
                            String hiddenFieldValue = hiddenFieldObj.getString("value");
                            element.put(hiddenField, hiddenFieldValue);
                        }
                    }
                } else if (CollectionUtils.isNotEmpty(hiddenFieldUniqueIdentifierList)) {
                    for (int i = 0; i < hiddenFieldUniqueIdentifierList.size(); i++) {
                        String hiddenFieldUniqueIdentifier = hiddenFieldUniqueIdentifierList.getString(i);
                        if (StringUtils.isBlank(hiddenFieldUniqueIdentifier)) {
                            continue;
                        }
                        String hiddenField = uniqueIdentifierToUuidMap.get(hiddenFieldUniqueIdentifier);
                        if (StringUtils.isBlank(hiddenField)) {
                            continue;
                        }
//                        if (Objects.equals(hiddenField, valueField)) {
//                            continue;
//                        }
                        JSONObject hiddenFieldObj = result.get(hiddenField);
                        if (MapUtils.isNotEmpty(hiddenFieldObj)) {
                            String hiddenFieldValue = hiddenFieldObj.getString("value");
                            element.put(hiddenFieldUniqueIdentifier, hiddenFieldValue);
                        }
                    }
                }
                dataList.add(element);
            }
        }
        JSONObject returnObj = new JSONObject();
        returnObj.put("dataList", dataList);
        returnObj.put("currentPage", dataVo.getCurrentPage());
        returnObj.put("pageSize", dataVo.getPageSize());
        returnObj.put("pageCount", dataVo.getPageCount());
        returnObj.put("rowNum", dataVo.getRowNum());
        return returnObj;
    }

    private void deduplicateData(List<Map<String, JSONObject>> previousPageList, String valueField, String textField, List<Map<String, JSONObject>> resultList) {
        List<String> duplicateValue = new ArrayList<>();
        List<String> duplicateText = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(previousPageList)) {
            for (Map<String, JSONObject> resultObj : previousPageList) {
                JSONObject firstObj = resultObj.get(valueField);
                if (MapUtils.isEmpty(firstObj)) {
                    continue;
                }
                JSONObject secondObj = resultObj.get(textField);
                if (MapUtils.isEmpty(secondObj)) {
                    continue;
                }
                String value = firstObj.getString("value");
                if (duplicateValue.contains(value)) {
                    continue;
                } else {
                    duplicateValue.add(value);
                }
                String text = secondObj.getString("text");
                if (!duplicateText.contains(text)) {
                    duplicateText.add(text);
                }
            }
        }
        Iterator<Map<String, JSONObject>> iterator = resultList.iterator();
        while (iterator.hasNext()) {
            Map<String, JSONObject> resultObj = iterator.next();
            JSONObject firstObj = resultObj.get(valueField);
            if (MapUtils.isEmpty(firstObj)) {
                iterator.remove();
                continue;
            }
            JSONObject secondObj = resultObj.get(textField);
            if (MapUtils.isEmpty(secondObj)) {
                iterator.remove();
                continue;
            }
            String value = firstObj.getString("value");
            if (StringUtils.isBlank(value)) {
                iterator.remove();
                continue;
            }
            if (duplicateValue.contains(value)) {
                iterator.remove();
                continue;
            } else {
                duplicateValue.add(value);
            }
            String text = secondObj.getString("text");
            if (StringUtils.isBlank(text)) {
                iterator.remove();
                continue;
            }
            if (duplicateText.contains(text)) {
                iterator.remove();
            } else {
                duplicateText.add(text);
            }
        }
    }
}
