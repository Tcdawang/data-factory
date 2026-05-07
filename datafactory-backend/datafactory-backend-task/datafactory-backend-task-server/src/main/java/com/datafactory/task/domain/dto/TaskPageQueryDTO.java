package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskPageQueryDTO {

    private Integer pageNo = 1;

    private Integer pageSize = 10;

    private String taskName;

    private String taskCode;

    private String status;

    private Long categoryId;
}
