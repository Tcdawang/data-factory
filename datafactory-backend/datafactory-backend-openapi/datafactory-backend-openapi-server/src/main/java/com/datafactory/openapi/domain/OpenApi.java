package com.datafactory.openapi.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OpenApi {
    private Long id;
    private String apiName;
    private String apiPath;
    private Long taskId;
    private String apiKey;
    private String status;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
