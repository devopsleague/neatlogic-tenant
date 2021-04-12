package codedriver.module.tenant.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyManager;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.core.RequestFrom;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.dao.mapper.MatrixExternalMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixColumnVo;
import codedriver.framework.matrix.dto.MatrixExternalVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.*;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.tenant.service.matrix.MatrixService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;

@Service
@Deprecated
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixExternalDataSearchApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(MatrixExternalDataSearchApi.class);

    @Resource
    private MatrixService matrixService;
    @Resource
    private IntegrationMapper integrationMapper;

    @Resource
    private MatrixMapper matrixMapper;

    @Resource
    private MatrixExternalMapper externalMapper;

    @Override
    public String getToken() {
        return "matrix/external/data/search";
    }

    @Override
    public String getName() {
        return "外部数据源数据检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
            @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
            @Param(name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)
    })
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "矩阵数据集合"),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "矩阵属性集合"),
            @Param(name = "referenceCount", type = ApiParamType.INTEGER, desc = "被引用次数"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "矩阵数据检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
        jsonObj.put("sourceColumnList", sourceColumnList); //防止集成管理 js length 异常
        MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo == null) {
            throw new MatrixNotFoundException(matrixUuid);
        }
        MatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
        JSONObject returnObj = new JSONObject();
        if (externalVo != null) {
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
            if (handler == null) {
                throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
            }

            integrationVo.getParamObj().putAll(jsonObj);
            IntegrationResultVo resultVo = handler.sendRequest(integrationVo, RequestFrom.MATRIX);
            if (StringUtils.isNotBlank(resultVo.getError())) {
                logger.error(resultVo.getError());
                throw new MatrixExternalAccessException();
            }
            try {
                handler.validate(resultVo);
            } catch (ApiRuntimeException ex) {
                logger.error(ex.getMessage());
                throw new ApiRuntimeException(ex.getMessage());
            }
            JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
            returnObj.putAll(transformedResult);
            JSONArray tbodyArray = transformedResult.getJSONArray("tbodyList");
            if (CollectionUtils.isNotEmpty(tbodyArray)) {
                List<Map<String, Object>> tbodyList = new ArrayList<>();
                for (int i = 0; i < tbodyArray.size(); i++) {
                    JSONObject rowData = tbodyArray.getJSONObject(i);
                    Integer pageSize = jsonObj.getInteger("pageSize");
                    pageSize = pageSize == null ? 10 : pageSize;
                    if (MapUtils.isNotEmpty(rowData)) {
                        Map<String, Object> rowDataMap = new HashMap<>();
                        for (Entry<String, Object> entry : rowData.entrySet()) {
                            rowDataMap.put(entry.getKey(), matrixService.matrixAttributeValueHandle(entry.getValue()));
                        }
                        tbodyList.add(rowDataMap);
                        if (tbodyList.size() >= pageSize) {
                            break;
                        }
                    }
                }
                returnObj.put("tbodyList", tbodyList);
            }
        } else {
            throw new MatrixExternalNotFoundException(matrixVo.getName());
        }

        int count = DependencyManager.getDependencyCount(CalleeType.MATRIX, matrixUuid);
        returnObj.put("referenceCount", count);
        return returnObj;
    }

}
