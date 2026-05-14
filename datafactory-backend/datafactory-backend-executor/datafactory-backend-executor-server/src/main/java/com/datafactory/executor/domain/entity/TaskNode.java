package com.datafactory.executor.domain.entity;

import lombok.Data;

@Data
public class TaskNode {
    private String nodeKey;
    private String nodeName;
    private String nodeType;
    private String configJson;
    private Integer positionX;
    private Integer positionY;
}
