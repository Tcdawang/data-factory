package com.datafactory.script.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComponentParam {

    private Long id;

    private Long componentId;

    private String paramName;

    private String paramKey;

    private String paramType;

    private Boolean required;

    private String defaultValue;

    private String options;

    private String placeholder;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}
