package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskVersionSaveDTO {
    private String versionNo;
    private String env;
    private String dagJson;
    private String dslJson;
    private String inputSchemaJson;
    private String outputSchemaJson;
    private String changeLog;
    private Long createdBy;
}
