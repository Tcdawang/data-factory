package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskCategoryUpdateDTO {

    private Long parentId;

    private String categoryName;

    private Integer sortNo;

    private Long updatedBy;
}
