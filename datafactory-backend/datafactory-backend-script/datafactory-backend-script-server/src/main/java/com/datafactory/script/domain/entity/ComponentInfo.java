package com.datafactory.script.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComponentInfo {

    private Long id;

    private String name;

    private String type;

    private String subType;

    private String description;

    private String icon;

    private String version;

    private String status;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer deleted;
}
