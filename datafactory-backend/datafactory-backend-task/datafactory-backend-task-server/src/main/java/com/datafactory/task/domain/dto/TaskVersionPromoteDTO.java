package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskVersionPromoteDTO {
    private String sourceEnv;
    private String targetEnv;
    private String changeLog;
    private Long operatorId;
}
