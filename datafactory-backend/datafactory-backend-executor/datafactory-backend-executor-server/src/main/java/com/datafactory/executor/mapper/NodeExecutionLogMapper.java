package com.datafactory.executor.mapper;

import com.datafactory.executor.domain.entity.NodeExecutionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NodeExecutionLogMapper {

    int insert(NodeExecutionLog log);

    NodeExecutionLog selectById(@Param("id") Long id);

    List<NodeExecutionLog> selectByExecutionId(@Param("executionId") String executionId);
}
