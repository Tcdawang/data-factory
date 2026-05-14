package com.datafactory.task.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskDagSaveDTO {
    private String dagJson;
    private String dslJson;
    private String inputSchemaJson;
    private String outputSchemaJson;
    private List<TaskNodeDTO> nodes;
    private List<TaskEdgeDTO> edges;
}
