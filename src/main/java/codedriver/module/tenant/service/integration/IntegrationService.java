/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.service.integration;

import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.integration.dto.table.ColumnVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author linbq
 * @since 2021/10/21 17:35
 **/
public interface IntegrationService {

    List<ColumnVo> getColumnList(IntegrationVo integrationVo);

    JSONArray getTheadList(IntegrationVo integrationVo, List<String> columnList);

    List<Map<String, Object>> getTbodyList(IntegrationResultVo resultVo, List<String> columnList);

//    void arrayColumnDataConversion(List<String> arrayColumnList, List<Map<String, Object>> tbodyList);
}
