/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.healthcheck;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.ADMIN;
import codedriver.framework.dto.healthcheck.DataSourceInfoVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.store.mysql.CodeDriverBasicDataSource;
import codedriver.framework.store.mysql.DatasourceManager;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetDatasourceInfoApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "/healthcheck/datasource";
    }

    @Override
    public String getName() {
        return "获取数据源信息";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Output({
            @Param(explode = DataSourceInfoVo.class),
    })
    @Description(desc = "获取数据源信息接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        CodeDriverBasicDataSource datasource = DatasourceManager.getDatasource();
        DataSourceInfoVo dataSourceInfoVo = new DataSourceInfoVo();
        dataSourceInfoVo.setPoolName(datasource.getPoolName());
        if (datasource.getHikariPoolMXBean() != null) {
            dataSourceInfoVo.setIdleConnections(datasource.getHikariPoolMXBean().getIdleConnections());
            dataSourceInfoVo.setActiveConnections(datasource.getHikariPoolMXBean().getActiveConnections());
            dataSourceInfoVo.setThreadsAwaitingConnection(datasource.getHikariPoolMXBean().getThreadsAwaitingConnection());
            dataSourceInfoVo.setTotalConnections(datasource.getHikariPoolMXBean().getTotalConnections());
        }
        return dataSourceInfoVo;
    }


}
