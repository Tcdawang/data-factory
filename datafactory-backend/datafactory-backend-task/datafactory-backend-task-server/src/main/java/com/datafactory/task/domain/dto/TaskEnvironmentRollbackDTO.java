package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskEnvironmentRollbackDTO {
    private String sourceEnv;
    private String targetEnv;
    private String reason;
    private Long operatorId;
}
