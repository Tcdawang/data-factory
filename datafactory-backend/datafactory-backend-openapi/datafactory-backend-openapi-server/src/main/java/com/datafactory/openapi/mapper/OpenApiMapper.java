package com.datafactory.openapi.mapper;

import com.datafactory.openapi.domain.OpenApi;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OpenApiMapper {
    int insert(OpenApi openApi);

    int update(OpenApi openApi);

    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("operatorId") Long operatorId);

    int delete(@Param("id") Long id, @Param("operatorId") Long operatorId);

    OpenApi selectById(@Param("id") Long id);

    OpenApi selectByPath(@Param("apiPath") String apiPath);

    List<OpenApi> list();
}
