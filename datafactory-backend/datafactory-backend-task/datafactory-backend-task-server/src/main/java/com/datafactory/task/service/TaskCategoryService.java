package com.datafactory.task.service;

import com.datafactory.task.domain.dto.TaskCategoryCreateDTO;
import com.datafactory.task.domain.dto.TaskCategoryUpdateDTO;
import com.datafactory.task.domain.vo.TaskCategoryVO;

import java.util.List;

public interface TaskCategoryService {

    Long create(TaskCategoryCreateDTO createDTO);

    void update(Long id, TaskCategoryUpdateDTO updateDTO);

    void delete(Long id, Long updatedBy);

    List<TaskCategoryVO> tree();
}
