package com.datafactory.task.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskEdge {
    private Long id;
    private Long taskId;
    private Long taskVersionId;
    private String sourceNodeKey;
    private String targetNodeKey;
    private String conditionExpr;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
