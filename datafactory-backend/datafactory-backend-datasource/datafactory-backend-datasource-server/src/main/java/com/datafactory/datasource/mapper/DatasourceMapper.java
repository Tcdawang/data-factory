package com.datafactory.datasource.mapper;

import com.datafactory.datasource.domain.Datasource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DatasourceMapper {
    int insert(Datasource datasource);

    int update(Datasource datasource);

    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("operatorId") Long operatorId);

    int delete(@Param("id") Long id, @Param("operatorId") Long operatorId);

    Datasource selectById(@Param("id") Long id);

    long countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    long countByJdbcUrl(@Param("jdbcUrl") String jdbcUrl, @Param("excludeId") Long excludeId);

    long count(@Param("name") String name, @Param("type") String type, @Param("status") String status);

    List<Datasource> page(@Param("name") String name, @Param("type") String type, @Param("status") String status, @Param("offset") long offset, @Param("pageSize") int pageSize);
}
