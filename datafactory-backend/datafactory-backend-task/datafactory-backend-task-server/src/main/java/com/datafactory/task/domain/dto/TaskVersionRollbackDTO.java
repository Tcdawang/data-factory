package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskVersionRollbackDTO {
    private String env;
    private Long targetVersionId;
    private String reason;
    private Long operatorId;
}
