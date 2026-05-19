package com.datafactory.script.domain.vo;

import lombok.Data;

@Data
public class ComponentTestResultVO {

    private Boolean success;

    private String message;

    private Object data;

    private Long executionTime;
}
