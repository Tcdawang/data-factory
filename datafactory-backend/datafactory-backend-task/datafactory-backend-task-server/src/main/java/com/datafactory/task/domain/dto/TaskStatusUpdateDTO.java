package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskStatusUpdateDTO {
    private String status;
    private Long operatorId;
}
