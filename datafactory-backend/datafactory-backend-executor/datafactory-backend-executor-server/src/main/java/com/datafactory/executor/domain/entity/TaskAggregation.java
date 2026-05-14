package com.datafactory.executor.domain.entity;

import lombok.Data;

@Data
public class TaskAggregation {

    private Long taskId;

    private String taskCode;

    private String taskName;

    private String environment;

    private String dslContent;

    private String version;
}
