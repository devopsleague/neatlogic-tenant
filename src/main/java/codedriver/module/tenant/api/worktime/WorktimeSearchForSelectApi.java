/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.tenant.api.worktime;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
import codedriver.framework.worktime.dto.WorktimeVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorktimeSearchForSelectApi extends PrivateApiComponentBase {

    @Resource
    private WorktimeMapper worktimeMapper;

    @Override
    public String getToken() {
        return "worktime/search/forselect";
    }

    @Override
    public String getName() {
        return "查询工作时间窗口列表_下拉框";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "模糊搜索", xss = true),
            @Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(name = "list", explode = ValueTextVo[].class, desc = "工作时间窗口列表")
    })
    @Description(desc = "查询工作时间窗口列表_下拉框")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorktimeVo worktimeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<WorktimeVo>() {
        });
        JSONObject resultObj = new JSONObject();
        if (worktimeVo.getNeedPage()) {
            int rowNum = worktimeMapper.searchWorktimeCount(worktimeVo);
            int pageCount = PageUtil.getPageCount(rowNum, worktimeVo.getPageSize());
            worktimeVo.setPageCount(pageCount);
            worktimeVo.setRowNum(rowNum);
            resultObj.put("currentPage", worktimeVo.getCurrentPage());
            resultObj.put("pageSize", worktimeVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
        }

        List<ValueTextVo> worktimeList = worktimeMapper.searchWorktimeListForSelect(worktimeVo);
        resultObj.put("list", worktimeList);
        return resultObj;
    }

}
