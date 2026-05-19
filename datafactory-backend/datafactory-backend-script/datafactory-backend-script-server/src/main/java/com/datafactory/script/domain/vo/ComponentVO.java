package com.datafactory.script.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComponentVO {

    private Long id;

    private String name;

    private String type;

    private String subType;

    private String description;

    private String icon;

    private String version;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
