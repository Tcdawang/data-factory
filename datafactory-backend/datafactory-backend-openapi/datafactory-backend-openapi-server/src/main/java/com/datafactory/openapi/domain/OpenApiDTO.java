package com.datafactory.openapi.domain;

import lombok.Data;

@Data
public class OpenApiDTO {
    private String apiName;
    private String apiPath;
    private Long taskId;
    private String apiKey;
    private String status;
    private Long operatorId;
}
