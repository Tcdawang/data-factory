package com.datafactory.script.domain.dto;

import lombok.Data;

@Data
public class ComponentPageQueryDTO {

    private String name;

    private String type;

    private String subType;

    private String status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
