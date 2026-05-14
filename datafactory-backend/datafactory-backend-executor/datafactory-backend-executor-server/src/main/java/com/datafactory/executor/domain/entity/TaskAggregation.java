package com.datafactory.executor.domain.entity;

import lombok.Data;

import java.util.List;

@Data
public class TaskAggregation {

    private Long taskId;

    private String taskCode;

    private String taskName;

    private String environment;

    private String dslContent;

    private Long versionId;

    private String version;

    private String dagJson;

    private List<TaskNode> nodes;

    private List<TaskEdge> edges;
}
