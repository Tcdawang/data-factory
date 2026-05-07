package com.datafactory.task.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskVersionCompareVO {
    private Long sourceVersionId;
    private String sourceVersionNo;
    private String sourceEnv;
    private String sourceDagJson;
    private String sourceDslJson;
    private String sourceInputSchemaJson;
    private String sourceOutputSchemaJson;
    private Long targetVersionId;
    private String targetVersionNo;
    private String targetEnv;
    private String targetDagJson;
    private String targetDslJson;
    private String targetInputSchemaJson;
    private String targetOutputSchemaJson;
    private Boolean dagChanged;
    private Boolean dslChanged;
    private Boolean inputSchemaChanged;
    private Boolean outputSchemaChanged;
}
