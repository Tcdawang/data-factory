package com.datafactory.task.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskVersionVO {
    private Long id;
    private Long taskId;
    private String versionNo;
    private String env;
    private String versionStatus;
    private String publishStatus;
    private String dagJson;
    private String dslJson;
    private String inputSchemaJson;
    private String outputSchemaJson;
    private String testStatus;
    private Long testExecutionId;
    private Long rollbackFromVersionId;
    private LocalDateTime publishTime;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
