package com.datafactory.task.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCategory {

    private Long id;

    private Long parentId;

    private String categoryName;

    private Integer sortNo;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}
