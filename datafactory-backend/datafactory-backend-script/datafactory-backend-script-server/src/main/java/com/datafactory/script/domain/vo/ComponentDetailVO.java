package com.datafactory.script.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ComponentDetailVO {

    private Long id;

    private String name;

    private String type;

    private String subType;

    private String description;

    private String icon;

    private String version;

    private String status;

    private List<ComponentParamVO> params;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
