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

    TaskVersion selectCurrentByTaskIdAndEnv(@Param("taskId") Long taskId, @Param("env") String env);

    int countByTaskIdAndEnv(@Param("taskId") Long taskId, @Param("env") String env);

    int updateDag(TaskVersion taskVersion);

    int updatePublishStatus(@Param("id") Long id,
                            @Param("versionStatus") String versionStatus,
                            @Param("publishStatus") String publishStatus,
                            @Param("changeLog") String changeLog);

    int clearCurrentByTaskIdAndEnv(@Param("taskId") Long taskId, @Param("env") String env);

    int setCurrent(@Param("id") Long id);

    int logicDelete(@Param("id") Long id);

    int updateTestStatus(@Param("id") Long id,
                         @Param("testStatus") String testStatus,
                         @Param("testExecutionId") Long testExecutionId);
}
