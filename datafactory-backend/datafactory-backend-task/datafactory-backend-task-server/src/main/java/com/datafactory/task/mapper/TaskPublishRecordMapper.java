package com.datafactory.task.mapper;

import com.datafactory.task.domain.entity.TaskPublishRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskPublishRecordMapper {
    int insert(TaskPublishRecord record);

    List<TaskPublishRecord> selectByTaskId(@Param("taskId") Long taskId);
}
