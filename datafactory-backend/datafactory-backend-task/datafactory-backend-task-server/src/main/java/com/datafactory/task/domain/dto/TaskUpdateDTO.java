package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskUpdateDTO {

    private String taskName;

    private String taskCode;

    private Long categoryId;

    private String description;

    private String status;

    private Long updatedBy;
}
