package com.datafactory.task.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskNode {
    private Long id;
    private Long taskId;
    private Long taskVersionId;
    private String nodeKey;
    private String nodeName;
    private String nodeType;
    private Long componentId;
    private Long refResourceId;
    private String refResourceType;
    private String configJson;
    private String inputMappingJson;
    private String outputSchemaJson;
    private Integer positionX;
    private Integer positionY;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
