package com.datafactory.executor.domain.entity;

import lombok.Data;

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
}
