package com.datafactory.datasource.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Datasource {
    private Long id;
    private String datasourceName;
    private String datasourceType;
    private String description;
    private String jdbcUrl;
    private String username;
    private String password;
    private String status;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
