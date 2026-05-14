package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskNodeDTO {
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
}
