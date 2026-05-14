package com.datafactory.executor.mapper;

import com.datafactory.executor.domain.entity.TaskExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaskExecutionMapper {

    int insert(TaskExecution taskExecution);

    TaskExecution selectById(@Param("id") Long id);

    TaskExecution selectByExecutionId(@Param("executionId") String executionId);

    int updateStatus(@Param("executionId") String executionId,
                     @Param("status") String status,
                     @Param("updatedBy") String updatedBy);
}
