package com.datafactory.script.domain.vo;

import lombok.Data;

@Data
public class ComponentParamVO {

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
}
