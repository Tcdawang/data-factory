package com.datafactory.openapi.domain;

import lombok.Data;

import java.util.List;

@Data
public class DashboardSummaryVO {
    private Long datasourceCount;
    private Long taskCount;
    private Long openApiCount;
    private Long todayExecutionCount;
    private Long last24hExecutionCount;
    private Long last24hSuccessCount;
    private Integer last24hSuccessRate;
    private List<RecentExecutionVO> recentExecutions;
}
