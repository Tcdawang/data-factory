package com.datafactory.task.mapper;

import com.datafactory.task.domain.dto.TaskPageQueryDTO;
import com.datafactory.task.domain.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {

    int insert(Task task);

    int updateById(Task task);

    int deleteById(@Param("id") Long id, @Param("updatedBy") Long updatedBy);

    Task selectById(@Param("id") Long id);

    Task selectByTaskName(@Param("taskName") String taskName, @Param("excludeId") Long excludeId);

    Task selectByTaskCode(@Param("taskCode") String taskCode, @Param("excludeId") Long excludeId);

    long countPage(TaskPageQueryDTO query);

    List<Task> selectPage(@Param("query") TaskPageQueryDTO query, @Param("offset") long offset, @Param("pageSize") int pageSize);
}
