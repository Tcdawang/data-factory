package com.datafactory.task.mapper;

import com.datafactory.task.domain.entity.TaskEdge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskEdgeMapper {
    int insertBatch(@Param("edges") List<TaskEdge> edges);

    int deleteByVersionId(@Param("taskVersionId") Long taskVersionId);

    List<TaskEdge> selectByVersionId(@Param("taskVersionId") Long taskVersionId);
}
