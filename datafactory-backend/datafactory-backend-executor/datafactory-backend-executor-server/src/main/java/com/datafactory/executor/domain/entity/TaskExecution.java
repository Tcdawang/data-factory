package com.datafactory.executor.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskExecution {

    private Long id;

    private Long taskId;

    private String taskCode;

    private String environment;

    private String executionId;

    private String status;

    private String triggerType;

    private String triggerUser;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String errorMessage;

    private Integer deleteFlag;

    private String createdBy;

    private LocalDateTime createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;
}
