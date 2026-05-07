package com.datafactory.task.service;

import com.datafactory.common.result.PageResult;
import com.datafactory.task.domain.dto.TaskCreateDTO;
import com.datafactory.task.domain.dto.TaskPageQueryDTO;
import com.datafactory.task.domain.dto.TaskUpdateDTO;
import com.datafactory.task.domain.vo.TaskVersionCompareVO;
import com.datafactory.task.domain.vo.TaskVersionVO;
import com.datafactory.task.domain.vo.TaskVO;

import java.util.List;

public interface TaskService {

    Long create(TaskCreateDTO createDTO);

    void update(Long id, TaskUpdateDTO updateDTO);

    void delete(Long id, Long updatedBy);

    TaskVO getDetail(Long id);

    PageResult<TaskVO> page(TaskPageQueryDTO queryDTO);

    List<TaskVersionVO> listVersions(Long taskId, String env);

    TaskVersionCompareVO compareVersions(Long taskId, Long sourceVersionId, Long targetVersionId);
}
