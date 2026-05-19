package com.datafactory.script.domain.dto;

import lombok.Data;

@Data
public class ComponentCreateDTO {

    private String name;

    private String type;

    private String subType;

    private String description;

    private String icon;

    private Long createdBy;
}
