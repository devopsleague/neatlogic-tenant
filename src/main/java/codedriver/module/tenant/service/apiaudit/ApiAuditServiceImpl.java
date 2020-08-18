package codedriver.module.tenant.service.apiaudit;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.core.ApiComponentFactory;
import codedriver.framework.restful.dao.mapper.ApiMapper;
import codedriver.framework.restful.dto.ApiAuditVo;
import codedriver.framework.restful.dto.ApiVo;
import codedriver.framework.util.AuditUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service
public class ApiAuditServiceImpl implements ApiAuditService{

    private static final String TIME_UINT_OF_DAY = "day";
    private static final String TIME_UINT_OF_MONTH = "month";

    @Autowired
    private ApiMapper apiMapper;

    @Override
    public List<ApiAuditVo> searchApiAuditVo(ApiAuditVo apiAuditVo) throws ClassNotFoundException {
        List<ApiVo> apiList = new ArrayList<>();
        assembleParamsAndFilterApi(apiAuditVo,apiList);
        if(CollectionUtils.isEmpty(apiAuditVo.getTokenList())){
            return null;
        }
        List<ApiAuditVo> apiAuditVoList = apiMapper.searchApiAuditList(apiAuditVo);
        if(apiAuditVo.getNeedPage()){
            apiAuditVo.setRowNum(apiMapper.searchApiAuditListCount(apiAuditVo));
        }
        /**
         * 补充从数据库无法获取的字段
         */
        addFields(apiList, apiAuditVoList);
        return apiAuditVoList;
    }

    @Override
    public List<ApiAuditVo> searchApiAuditForExport(ApiAuditVo apiAuditVo) throws Exception {
        List<ApiVo> apiList = new ArrayList<>();
        assembleParamsAndFilterApi(apiAuditVo,apiList);
        if(CollectionUtils.isEmpty(apiAuditVo.getTokenList())){
            return null;
        }
        List<ApiAuditVo> apiAuditList = apiMapper.searchApiAuditForExport(apiAuditVo);
        /**
         * 读取文件中的参数/结果/错误
         */
        if(CollectionUtils.isNotEmpty(apiAuditList)){
            for(ApiAuditVo vo : apiAuditList){
                String paramFilePath = vo.getParamFilePath();
                String resultFilePath = vo.getResultFilePath();
                String errorFilePath = vo.getErrorFilePath();

                if(StringUtils.isNotBlank(paramFilePath)){
                    long offset = Long.parseLong(paramFilePath.split("\\?")[1].split("&")[1].split("=")[1]);
                    if(offset > AuditUtil.maxFileSize){
                        vo.setParam("内容过长，不予导出");
                    }else{
                        String param = AuditUtil.getAuditDetail(paramFilePath);
                        vo.setParam(param);
                    }
                }
                if(StringUtils.isNotBlank(resultFilePath)){
                    long offset = Long.parseLong(resultFilePath.split("\\?")[1].split("&")[1].split("=")[1]);
                    if(offset > AuditUtil.maxFileSize){
                        vo.setResult("内容过长，不予导出");
                    }else{
                        String result = AuditUtil.getAuditDetail(resultFilePath);
                        vo.setResult(result);
                    }
                }
                if(StringUtils.isNotBlank(errorFilePath)){
                    long offset = Long.parseLong(errorFilePath.split("\\?")[1].split("&")[1].split("=")[1]);
                    if(offset > AuditUtil.maxFileSize){
                        vo.setError("内容过长，不予导出");
                    }else{
                        String error = AuditUtil.getAuditDetail(errorFilePath);
                        vo.setError(error);
                    }
                }
            }
        }
        /**
         * 补充从数据库无法获取的字段
         */
        addFields(apiList, apiAuditList);

        return apiAuditList;
    }

    @Override
    public int searchApiAuditVoCount(ApiAuditVo vo) throws ClassNotFoundException {
        List<ApiVo> apiList = new ArrayList<>();
        assembleParamsAndFilterApi(vo,apiList);
        if(CollectionUtils.isEmpty(vo.getTokenList())){
            return 0;
        }
        int apiAuditVoCount = apiMapper.searchApiAuditListCount(vo);
        return apiAuditVoCount;

    }

    /**
     * 筛选出api_audit表中有记录的API
     * @return
     */
    @Override
    public List<ApiVo> getApiListForTree() {
        List<String> distinctTokenInApiAudit = apiMapper.getDistinctTokenInApiAudit();
        List<ApiVo> apiList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(distinctTokenInApiAudit)){
            Map<String, ApiVo> ramApiMap = ApiComponentFactory.getApiMap();
            List<ApiVo> dbApiList = apiMapper.getAllApi();
            if(CollectionUtils.isNotEmpty(dbApiList)){
                List<ApiVo> customApiVos = new ArrayList<>();
                for (ApiVo vo : dbApiList) {
                    if (ramApiMap.get(vo.getToken()) == null) {
                        customApiVos.add(vo);
                    }
                }
                /** 从内存和数据库中筛选api，如果内存中找不到，那就从数据库中找*/
                for (String token : distinctTokenInApiAudit) {
                    ApiVo apiVo = ramApiMap.get(token);
                    if (apiVo != null) {
                        apiList.add(apiVo);
                    }else {
                        for (ApiVo vo : customApiVos) {
                            if (vo.getToken().equals(token)) {
                                apiList.add(vo);
                            }
                        }
                    }
                }
            }
        }
        return apiList;
    }

    private void assembleParamsAndFilterApi(ApiAuditVo apiAuditVo, List<ApiVo> apiList) throws ClassNotFoundException {

        if(StringUtils.isNotBlank(apiAuditVo.getKeyword()) && apiAuditVo.getKeyword().contains(".")){
            apiAuditVo.setIp(apiAuditVo.getKeyword());
        }

        /**
         * 如果选择按下拉框上的时间跨度筛选，那么就要计算出筛选的起止时间
         */
        if (apiAuditVo.getTimeRange() != null && StringUtils.isNotBlank(apiAuditVo.getTimeUnit())) {
            if (TIME_UINT_OF_DAY.equals(apiAuditVo.getTimeUnit())) {
                apiAuditVo.setStartTime(DateUtils.addDays(Calendar.getInstance().getTime(), -apiAuditVo.getTimeRange()));
            } else if (TIME_UINT_OF_MONTH.equals(apiAuditVo.getTimeUnit())) {
                apiAuditVo.setStartTime(DateUtils.addMonths(Calendar.getInstance().getTime(), -apiAuditVo.getTimeRange()));
            }
            apiAuditVo.setEndTime(Calendar.getInstance().getTime());
        }

        /** 首先筛选出api_audit表中有记录的API，再用这些API做进一步的筛选 */
        List<ApiVo> apiVoList = getApiListForTree();
//        List<ApiVo> dbApiList = apiMapper.getAllApi();
//        Map<String, ApiVo> ramApiMap = ApiComponentFactory.getApiMap();
//        for (ApiVo vo : dbApiList) {
//            if (ramApiMap.get(vo.getToken()) == null) {
//                apiVoList.add(vo);
//            }
//        }
//        List<ApiVo> apiList = new ArrayList<>();
        List<String> apiTokenList = new ArrayList<>();
        for (ApiVo api : apiVoList) {
            //根据模块筛选接口
            if (StringUtils.isNotBlank(apiAuditVo.getModuleGroup()) && !apiAuditVo.getModuleGroup().equals(api.getModuleGroup())) {
                continue;
            }
            //根据功能筛选接口
//            if (StringUtils.isNotBlank(apiAuditVo.getFuncId()) && !api.getToken().contains(apiAuditVo.getFuncId())) {
//                continue;
//            }
            if (StringUtils.isNotBlank(apiAuditVo.getFuncId())) {
                if(api.getToken().contains("/")){
                    if(!api.getToken().startsWith(apiAuditVo.getFuncId() + "/")){
                        continue;
                    }
                }else{
                    if(!api.getToken().equals(apiAuditVo.getFuncId())){
                        continue;
                    }
                }

            }
            if (StringUtils.isNotBlank(apiAuditVo.getKeyword()) && !apiAuditVo.getKeyword().contains(".")) {
                if (!api.getName().contains(apiAuditVo.getKeyword()) && !api.getToken().contains(apiAuditVo.getKeyword())) {
                    continue;
                }
            }
            //TODO 根据操作类型筛选
            if(StringUtils.isNotBlank(apiAuditVo.getOperationType())){
                Class<?> apiClass = Class.forName(api.getHandler());
                OperationType annotation = apiClass.getAnnotation(OperationType.class);
                if(annotation == null || !apiAuditVo.getOperationType().equals(annotation.type().getValue())){
                    continue;
                }
            }
            apiList.add(api);
            apiTokenList.add(api.getToken());
        }
        //把筛选出来的api的token塞到apiAuditVo，以便之后的数据库查询
        apiAuditVo.setTokenList(apiTokenList);
    }

    private void addFields(List<ApiVo> apiList, List<ApiAuditVo> apiAuditVoList) throws ClassNotFoundException {
        if(CollectionUtils.isNotEmpty(apiList) && CollectionUtils.isNotEmpty(apiAuditVoList)){
            for (ApiAuditVo vo : apiAuditVoList) {
                for (ApiVo api : apiList) {
                    if (vo.getToken().equals(api.getToken())) {
                        vo.setApiName(api.getName());
                        vo.setModuleGroup(api.getModuleGroup());
                        Class<?> apiClass = Class.forName(api.getHandler());
                        OperationType annotation = apiClass.getAnnotation(OperationType.class);
                        if (annotation != null) {
                            vo.setOperationType(annotation.type().getValue());
                        }else{
                            //如果API没有加操作类型的注解，那么默认视为SEARCH
                            vo.setOperationType(OperationTypeEnum.SEARCH.getValue());
                        }
                        break;
                    }
                }
            }
        }

    }
}