package com.datafactory.task.mapper;

import com.datafactory.task.domain.entity.TaskVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskVersionMapper {
    int insert(TaskVersion taskVersion);

    TaskVersion selectById(@Param("id") Long id);

    List<TaskVersion> selectByTaskId(@Param("taskId") Long taskId);

    List<TaskVersion> selectByTaskIdAndEnv(@Param("taskId") Long taskId, @Param("env") String env);

    TaskVersion selectLatestByTaskIdAndEnv(@Param("taskId") Long taskId, @Param("env") String env);
}
