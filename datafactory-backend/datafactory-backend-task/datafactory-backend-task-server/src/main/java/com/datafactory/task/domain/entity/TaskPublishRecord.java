package com.datafactory.task.domain.entity;

import lombok.Data;

@Data
public class TaskPublishRecord {
    private Long id;
    private Long taskId;
    private Long taskVersionId;
    private String sourceEnv;
    private String targetEnv;
    private String publishType;
    private Long beforeVersionId;
    private Long afterVersionId;
    private String remark;
    private Long operatorId;
}
