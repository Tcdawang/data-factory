package com.datafactory.openapi.domain;

import lombok.Data;

@Data
public class OpenApiCallLogVO {
    private Long id;
    private Long openApiId;
    private String apiName;
    private String apiPath;
    private Long taskId;
    private String executionId;
    private String requestJson;
    private String responseJson;
    private String status;
    private String errorMessage;
    private String createdAt;
}
