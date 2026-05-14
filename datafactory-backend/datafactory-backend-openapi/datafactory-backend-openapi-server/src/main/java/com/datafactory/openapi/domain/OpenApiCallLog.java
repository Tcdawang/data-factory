package com.datafactory.openapi.domain;

import lombok.Data;

@Data
public class OpenApiCallLog {
    private Long id;
    private Long openApiId;
    private String apiPath;
    private Long taskId;
    private String executionId;
    private String requestJson;
    private String responseJson;
    private String status;
    private String errorMessage;
}
