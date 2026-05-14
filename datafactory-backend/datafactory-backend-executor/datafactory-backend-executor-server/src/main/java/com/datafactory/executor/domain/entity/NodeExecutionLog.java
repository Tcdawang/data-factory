package com.datafactory.executor.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NodeExecutionLog {

    private Long id;

    private String executionId;

    private String nodeId;

    private String nodeName;

    private String nodeType;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String inputData;

    private String outputData;

    private String errorMessage;

    private LocalDateTime createdTime;
}
