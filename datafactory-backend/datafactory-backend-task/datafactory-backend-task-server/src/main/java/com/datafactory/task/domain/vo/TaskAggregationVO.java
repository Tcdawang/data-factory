package com.datafactory.task.domain.vo;

import com.datafactory.task.domain.entity.TaskEdge;
import com.datafactory.task.domain.entity.TaskNode;
import lombok.Data;

import java.util.List;

@Data
public class TaskAggregationVO {
    private Long taskId;
    private String taskCode;
    private String taskName;
    private String environment;
    private Long versionId;
    private String version;
    private String dslContent;
    private String dagJson;
    private List<TaskNode> nodes;
    private List<TaskEdge> edges;
}
