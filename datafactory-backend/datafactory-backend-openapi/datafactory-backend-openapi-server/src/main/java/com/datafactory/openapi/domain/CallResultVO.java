package com.datafactory.openapi.domain;

import lombok.Data;

@Data
public class CallResultVO {
    private Long taskId;
    private String executionId;
    private String status;
}
