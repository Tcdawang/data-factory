package com.datafactory.executor.domain.dto;

import lombok.Data;

@Data
public class TaskVersionTestStatusUpdateDTO {
    private String testStatus;
    private Long testExecutionId;
}
