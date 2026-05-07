package com.datafactory.task.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskVO {

    private Long id;

    private String taskName;

    private String taskCode;

    private Long categoryId;

    private String description;

    private String status;

    private Long currentVersionId;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private TaskVersionVO latestVersion;
}
