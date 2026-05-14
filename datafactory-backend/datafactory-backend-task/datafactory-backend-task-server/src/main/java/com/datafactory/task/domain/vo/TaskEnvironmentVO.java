package com.datafactory.task.domain.vo;

import lombok.Data;

@Data
public class TaskEnvironmentVO {
    private String env;
    private String envName;
    private TaskVersionVO currentVersion;
    private TaskVersionVO latestVersion;
    private Integer versionCount;
}
