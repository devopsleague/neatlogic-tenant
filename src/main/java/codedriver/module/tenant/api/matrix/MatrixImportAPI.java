package codedriver.module.tenant.api.matrix;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.matrix.dao.mapper.MatrixDataMapper;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixColumnVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixDataNotFoundException;
import codedriver.framework.matrix.exception.MatrixFileNotFoundException;
import codedriver.framework.matrix.exception.MatrixHeaderMisMatchException;
import codedriver.framework.matrix.exception.MatrixImportException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.UuidUtil;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-01 16:32
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixImportAPI extends PrivateBinaryStreamApiComponentBase {

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Autowired
    private MatrixDataMapper dataMapper;

    @Override
    public String getToken() {
        return "matrix/import";
    }

    @Override
    public String getName() {
        return "矩阵导入接口";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({ 
    	@Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)
    })
    @Description(desc = "矩阵导入接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String matrixUuid = paramObj.getString("matrixUuid");
	    MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
	    if(matrixVo == null) {
	    	throw new MatrixNotFoundException(matrixUuid);
	    }
	    if(MatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
	    	JSONObject returnObj = new JSONObject();
	        int update = 0, insert = 0, unExist = 0;
	        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
	        //获取所有导入文件
	        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
	        if(multipartFileMap == null || multipartFileMap.isEmpty()) {
	            throw new MatrixFileNotFoundException();
	        }
	        MultipartFile multipartFile = null;
	        InputStream is = null;
	        for(Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
	        	multipartFile = entry.getValue();
	        	is = multipartFile.getInputStream();
	            String originalFilename = multipartFile.getOriginalFilename();
	            if(originalFilename.indexOf(".") != -1) {
	            	originalFilename = originalFilename.substring(0, originalFilename.indexOf("."));
	            }   
                if(!originalFilename.equals(matrixVo.getName())) {
                	throw new MatrixImportException("文件的名称与矩阵名称不相同，不能导入");
                }

                List<MatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixVo.getUuid());
                if (CollectionUtils.isNotEmpty(attributeVoList)){
                    Map<String, String> headerMap = new HashMap<>();
                    for (MatrixAttributeVo attributeVo : attributeVoList){
                        headerMap.put(attributeVo.getName(), attributeVo.getUuid());
                    }
                    if (is != null){
                        Workbook wb = WorkbookFactory.create(is);
                        Sheet sheet = wb.getSheetAt(0);
                        int rowNum = sheet.getLastRowNum();
                        //获取头栏位
                        Row headerRow = sheet.getRow(0);
                        int colNum = headerRow.getLastCellNum();
                        //attributeList 缺少uuid
                        if (colNum != attributeVoList.size() + 1){
                            throw new MatrixHeaderMisMatchException(originalFilename);
                        }
                        //解析数据
                        for (int i = 1; i <= rowNum; i++){
                            Row row = sheet.getRow(i);
                            boolean isNew = false;
                            MatrixColumnVo uuidColumn = null;
                            List<MatrixColumnVo> rowData = new ArrayList<>();
                            for (int j = 0; j < colNum; j++){
                                Cell tbodycell = row.getCell(j);
                                String value = getCellValue(tbodycell);
                                String attributeUuid = null;
                            	Cell theadCell = headerRow.getCell(j);
                                String columnName = theadCell.getStringCellValue();
                                if (("uuid").equals(columnName)){
                                	attributeUuid = "uuid";
                                	if(StringUtils.isBlank(value) || dataMapper.getDynamicTableDataCountByUuid(value, matrixVo.getUuid()) == 0) {
                                		value = UuidUtil.randomUuid();
                                		isNew = true;
                                		rowData.add(new MatrixColumnVo(attributeUuid, value));
                                	}else {
                                		uuidColumn = new MatrixColumnVo(attributeUuid, value);
                                	}
                                }else {
                                	attributeUuid = headerMap.get(columnName);
                                	if(StringUtils.isNotBlank(attributeUuid)) {
                                        rowData.add(new MatrixColumnVo(attributeUuid, value));
                                    }
                                }
                            }
                            if(isNew) {
                            	dataMapper.insertDynamicTableData(rowData, matrixUuid);
                            	insert++;
                            	update++;
                            }else {
                            	dataMapper.updateDynamicTableDataByUuid(rowData, uuidColumn, matrixUuid);
                            }
                        }
                    }
                }else {
                    throw new MatrixDataNotFoundException(originalFilename);
                }
	        }
	        returnObj.put("insert", insert);
	        returnObj.put("update", update);
	        returnObj.put("unExist", unExist);
	        return returnObj;
	    }else {
	    	throw new MatrixImportException("外部数据源不支持导入");
	    }       
    }

    private String getCellValue (Cell cell){
        String value = "";
        if (cell != null){
            if (cell.getCellType() != Cell.CELL_TYPE_BLANK){
                switch (cell.getCellType()){
                    case Cell.CELL_TYPE_NUMERIC :
                        if (DateUtil.isCellDateFormatted(cell)){
                            value = formatter.format(cell.getDateCellValue());
                        }else {
                            value = String.valueOf(cell.getNumericCellValue());
                        }
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        value = String.valueOf(cell.getBooleanCellValue());
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        value = cell.getCellFormula();
                        break;
                    default:
                        value = cell.getStringCellValue();
                        break;
                }
            }
        }
        return value;
    }
}
