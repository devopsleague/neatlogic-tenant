package codedriver.module.tenant.api.dashboard;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dao.mapper.DashboardMapper;
import codedriver.framework.dashboard.dto.DashboardVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.tenant.auth.label.DASHBOARD_MODIFY;
import codedriver.module.tenant.exception.dashboard.DashboardAuthenticationException;
import codedriver.module.tenant.exception.dashboard.DashboardNotFoundException;
import codedriver.module.tenant.exception.dashboard.DashboardParamException;

@Service
@Transactional
@IsActived
public class DashboardSaveApi extends ApiComponentBase {

	@Autowired
	private DashboardMapper dashboardMapper;

	@Autowired
	UserMapper userMapper;	
	@Autowired
	RoleMapper roleMapper;
	
	@Override
	public String getToken() {
		return "dashboard/save";
	}

	@Override
	public String getName() {
		return "仪表板修改保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "uuid", type = ApiParamType.STRING, desc = "仪表板uuid，为空代表新增", isRequired = true), 
			@Param(name = "name", xss = true, type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "仪表板名称", isRequired = true),
			@Param(name = "widgetList", type = ApiParamType.JSONARRAY, desc = "组件列表，范例：\"chartType\": \"barchart\"," + "\"h\": 4," + "\"handler\": \"codedriver.module.process.dashboard.handler.ProcessTaskDashboardHandler\"," + "\"i\": 0," + "\"name\": \"组件1\"," + "\"refreshInterval\": 3," + "\"uuid\": \"aaaa\"," + "\"w\": 5," + "\"x\": 0," + "\"y\": 0") })
	@Output({  })
	@Description(desc = "仪表板修改保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		DashboardVo dashboardVo = JSONObject.toJavaObject(jsonObj, DashboardVo.class);
		String userUuid = UserContext.get().getUserUuid(true);
		DashboardVo oldDashboardVo = dashboardMapper.getDashboardByUuid(dashboardVo.getUuid());
		if(oldDashboardVo == null) {
			throw new DashboardNotFoundException(dashboardVo.getUuid());
		}
		if(DashboardVo.DashBoardType.SYSTEM.getValue().equals(oldDashboardVo.getType())) {
			//判断是否有管理员权限
			if(CollectionUtils.isEmpty(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userUuid, DASHBOARD_MODIFY.class.getSimpleName())))) {
				throw new DashboardAuthenticationException("管理");
			}
		}else if(!oldDashboardVo.getFcu().equals(userUuid)){
			throw new DashboardAuthenticationException("修改");
		}
		//修改dashboard
		oldDashboardVo.setName(dashboardVo.getName());
		oldDashboardVo.setLcu(userUuid);
		dashboardMapper.updateDashboard(oldDashboardVo);
		//更新组件配置
		dashboardMapper.deleteDashboardWidgetByDashboardUuid(oldDashboardVo.getUuid());
		List<DashboardWidgetVo> dashboardWidgetList = dashboardVo.getWidgetList();
		if(CollectionUtils.isNotEmpty(dashboardWidgetList)) {
			for(DashboardWidgetVo widgetVo : dashboardWidgetList) {
				if(StringUtils.isBlank(widgetVo.getChartConfig())) {
					throw new DashboardParamException("widgetList.chartConfig");
				}
				widgetVo.setDashboardUuid(dashboardVo.getUuid());
				dashboardMapper.insertDashboardWidget(widgetVo);
			}
		}
		return null;
	}
}
