package com.datafactory.openapi.domain;

import lombok.Data;

@Data
public class OpenApiCallLogQueryDTO {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String apiPath;
    private String status;
    private Long taskId;
    private String executionId;
}
