package com.datafactory.openapi.controller;

import com.datafactory.common.result.Result;
import com.datafactory.openapi.domain.DashboardSummaryVO;
import com.datafactory.openapi.mapper.DashboardMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardMapper dashboardMapper;

    public DashboardController(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    @GetMapping("/summary")
    public Result<DashboardSummaryVO> summary() {
        DashboardSummaryVO summary = new DashboardSummaryVO();
        summary.setDatasourceCount(defaultZero(dashboardMapper.countDatasources()));
        summary.setTaskCount(defaultZero(dashboardMapper.countTasks()));
        summary.setOpenApiCount(defaultZero(dashboardMapper.countOpenApis()));
        summary.setTodayExecutionCount(defaultZero(dashboardMapper.countTodayExecutions()));
        long last24hTotal = defaultZero(dashboardMapper.countLast24hExecutions());
        long last24hSuccess = defaultZero(dashboardMapper.countLast24hSuccessExecutions());
        summary.setLast24hExecutionCount(last24hTotal);
        summary.setLast24hSuccessCount(last24hSuccess);
        summary.setLast24hSuccessRate(last24hTotal == 0 ? 0 : (int) Math.round(last24hSuccess * 100.0 / last24hTotal));
        summary.setRecentExecutions(dashboardMapper.selectRecentExecutions(10));
        return Result.success(summary);
    }

    private long defaultZero(Long value) {
        return value == null ? 0L : value;
    }
}
