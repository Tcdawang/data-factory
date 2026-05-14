package com.datafactory.openapi.domain;

import lombok.Data;

@Data
public class RecentExecutionVO {
    private Long id;
    private Long taskId;
    private String taskCode;
    private String taskName;
    private String environment;
    private String executionId;
    private String status;
    private String triggerType;
    private String startTime;
    private String endTime;
    private String createdTime;
}
