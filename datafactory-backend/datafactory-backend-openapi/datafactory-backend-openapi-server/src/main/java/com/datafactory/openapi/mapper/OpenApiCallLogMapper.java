package com.datafactory.openapi.mapper;

import com.datafactory.openapi.domain.OpenApiCallLogQueryDTO;
import com.datafactory.openapi.domain.OpenApiCallLogVO;
import com.datafactory.openapi.domain.OpenApiCallLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OpenApiCallLogMapper {
    int insert(OpenApiCallLog callLog);

    long count(@Param("query") OpenApiCallLogQueryDTO queryDTO);

    List<OpenApiCallLogVO> page(@Param("query") OpenApiCallLogQueryDTO queryDTO,
                                @Param("offset") long offset,
                                @Param("pageSize") int pageSize);
}
