package com.datafactory.task.mapper;

import com.datafactory.task.domain.entity.TaskCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskCategoryMapper {

    int insert(TaskCategory category);

    TaskCategory selectByCategoryName(@Param("categoryName") String categoryName, @Param("parentId") Long parentId);

    List<TaskCategory> selectAll();
}
