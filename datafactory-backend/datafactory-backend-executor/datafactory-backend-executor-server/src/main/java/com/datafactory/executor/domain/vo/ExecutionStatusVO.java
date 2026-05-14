package com.datafactory.executor.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExecutionStatusVO {

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

    private LocalDateTime createdTime;
}
