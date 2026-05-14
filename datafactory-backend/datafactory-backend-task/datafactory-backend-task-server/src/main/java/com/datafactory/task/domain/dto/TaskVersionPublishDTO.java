package com.datafactory.task.domain.dto;

import lombok.Data;

@Data
public class TaskVersionPublishDTO {
    private String changeLog;
    private Long operatorId;
}
