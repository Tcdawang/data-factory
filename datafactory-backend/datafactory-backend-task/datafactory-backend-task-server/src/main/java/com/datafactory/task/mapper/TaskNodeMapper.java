package com.datafactory.task.mapper;

import com.datafactory.task.domain.entity.TaskNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskNodeMapper {
    int insertBatch(@Param("nodes") List<TaskNode> nodes);

    int deleteByVersionId(@Param("taskVersionId") Long taskVersionId);

    List<TaskNode> selectByVersionId(@Param("taskVersionId") Long taskVersionId);
}
