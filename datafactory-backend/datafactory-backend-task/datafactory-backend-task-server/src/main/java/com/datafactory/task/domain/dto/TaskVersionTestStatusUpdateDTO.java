package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskVersionTestStatusUpdateDTO {
    private String testStatus;
    private Long testExecutionId;
}
