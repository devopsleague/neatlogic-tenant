package codedriver.module.tenant.api.matrix;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.matrix.exception.MatrixNameRepeatException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.UuidUtil;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:03
 **/
@Service
@Transactional
@AuthAction(name = "MATRIX_MODIFY")
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixCopyApi extends PrivateApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Override
    public String getToken() {
        return "matrix/copy";
    }

    @Override
    public String getName() {
        return "矩阵数据源复制接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ 
    	@Param(name = "uuid", desc = "矩阵数据源uuid", isRequired = true, type = ApiParamType.STRING),
        @Param(name = "name", desc = "矩阵名称", isRequired = true, type = ApiParamType.STRING)
    })
    @Description(desc = "矩阵数据源复制接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String uuid = jsonObj.getString("uuid");
        MatrixVo sourceMatrix = matrixMapper.getMatrixByUuid(uuid);
    	if(sourceMatrix == null) {
    		throw new MatrixNotFoundException(uuid);
    	}
    	if(MatrixType.CUSTOM.getValue().equals(sourceMatrix.getType())) {
    		String name = jsonObj.getString("name");
            //判断name是否存在
            String targetMatrixUuid = UuidUtil.randomUuid();
            sourceMatrix.setUuid(targetMatrixUuid);
            sourceMatrix.setName(name);
        	if(matrixMapper.checkMatrixNameIsRepeat(sourceMatrix) > 0){
        		throw new MatrixNameRepeatException(name);
        	}
            sourceMatrix.setFcu(UserContext.get().getUserUuid(true));
            sourceMatrix.setLcu(UserContext.get().getUserUuid(true));
            matrixMapper.insertMatrix(sourceMatrix);

            List<MatrixAttributeVo> attributeVoList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(uuid);
            if (CollectionUtils.isNotEmpty(attributeVoList)){
                //属性拷贝
                List<String> sourceColumnList = new ArrayList<>();
                List<String> targetColumnList = new ArrayList<>();
                for (MatrixAttributeVo attributeVo : attributeVoList){
                    String sourceAttributeUuid = attributeVo.getUuid();
                    String targetAttributeUuid = UuidUtil.randomUuid();
                    sourceColumnList.add(sourceAttributeUuid);
                    targetColumnList.add(targetAttributeUuid);
                    attributeVo.setMatrixUuid(targetMatrixUuid);
                    attributeVo.setUuid(targetAttributeUuid);
                    matrixAttributeMapper.insertMatrixAttribute(attributeVo);
                }

                if (matrixAttributeMapper.checkMatrixAttributeTableExist("matrix_" + targetMatrixUuid) == 0){
                    matrixAttributeMapper.createMatrixDynamicTable(attributeVoList, targetMatrixUuid);
                }
                //数据拷贝
                matrixDataMapper.insertDynamicTableDataForCopy(uuid, sourceColumnList, targetMatrixUuid, targetColumnList);
            }
    	}else {
    		throw new MatrixExternalException("矩阵外部数据源没有复制操作");
    	}
    	
        return null;
    }
}