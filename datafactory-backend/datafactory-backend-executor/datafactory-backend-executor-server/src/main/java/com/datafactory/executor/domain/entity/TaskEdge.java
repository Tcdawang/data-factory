package com.datafactory.executor.domain.entity;

import lombok.Data;

@Data
public class TaskEdge {
    private String sourceNodeKey;
    private String targetNodeKey;
    private String conditionExpr;
    private Integer sortOrder;
}
