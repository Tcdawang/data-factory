package com.datafactory.datasource.service;

import com.datafactory.common.result.PageResult;
import com.datafactory.datasource.domain.Datasource;
import com.datafactory.datasource.domain.DatasourceDTO;
import com.datafactory.datasource.domain.DatasourceQueryDTO;
import com.datafactory.datasource.domain.TestResultVO;

public interface DatasourceService {
    Long create(DatasourceDTO dto);

    void update(Long id, DatasourceDTO dto);

    void delete(Long id, Long operatorId);

    void publish(Long id, Long operatorId);

    void disable(Long id, Long operatorId);

    Datasource get(Long id);

    PageResult<Datasource> page(DatasourceQueryDTO queryDTO);

    TestResultVO test(Long id);
}
