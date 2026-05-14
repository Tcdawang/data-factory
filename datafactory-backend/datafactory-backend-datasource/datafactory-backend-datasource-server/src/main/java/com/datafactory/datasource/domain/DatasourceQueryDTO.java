package com.datafactory.datasource.domain;

import lombok.Data;

@Data
public class DatasourceQueryDTO {
    private String datasourceName;
    private String datasourceType;
    private String status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
