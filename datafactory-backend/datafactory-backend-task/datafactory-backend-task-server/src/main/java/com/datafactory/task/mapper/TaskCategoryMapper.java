package com.datafactory.task.mapper;

import com.datafactory.task.domain.entity.TaskCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskCategoryMapper {

    int insert(TaskCategory category);

    TaskCategory selectByCategoryName(@Param("categoryName") String categoryName, @Param("parentId") Long parentId);

    TaskCategory selectByCategoryNameExcludeId(@Param("categoryName") String categoryName,
                                               @Param("parentId") Long parentId,
                                               @Param("excludeId") Long excludeId);

    TaskCategory selectById(@Param("id") Long id);

    List<TaskCategory> selectAll();

    long countChildren(@Param("parentId") Long parentId);

    long countTasks(@Param("categoryId") Long categoryId);

    int update(TaskCategory category);

    int delete(@Param("id") Long id, @Param("updatedBy") Long updatedBy);
}
