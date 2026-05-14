package com.datafactory.datasource.domain;

import lombok.Data;

@Data
public class DatasourceDTO {
    private String datasourceName;
    private String datasourceType;
    private String description;
    private String jdbcUrl;
    private String username;
    private String password;
    private String status;
    private Long operatorId;
}
