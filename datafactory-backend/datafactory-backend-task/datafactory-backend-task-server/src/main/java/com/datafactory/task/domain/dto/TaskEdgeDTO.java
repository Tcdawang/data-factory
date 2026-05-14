package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskEdgeDTO {
    private String sourceNodeKey;
    private String targetNodeKey;
    private String conditionExpr;
    private Integer sortOrder;
}
