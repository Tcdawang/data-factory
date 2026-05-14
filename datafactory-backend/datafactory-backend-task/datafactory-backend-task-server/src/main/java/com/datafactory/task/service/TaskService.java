package com.datafactory.task.service;

import com.datafactory.common.result.PageResult;
import com.datafactory.task.domain.dto.TaskCreateDTO;
import com.datafactory.task.domain.dto.TaskDagSaveDTO;
import com.datafactory.task.domain.dto.TaskPageQueryDTO;
import com.datafactory.task.domain.dto.TaskStatusUpdateDTO;
import com.datafactory.task.domain.dto.TaskUpdateDTO;
import com.datafactory.task.domain.dto.TaskVersionPromoteDTO;
import com.datafactory.task.domain.dto.TaskVersionPublishDTO;
import com.datafactory.task.domain.dto.TaskVersionRollbackDTO;
import com.datafactory.task.domain.dto.TaskVersionSaveDTO;
import com.datafactory.task.domain.dto.TaskVersionTestStatusUpdateDTO;
import com.datafactory.task.domain.dto.TaskEnvironmentRollbackDTO;
import com.datafactory.task.domain.vo.TaskAggregationVO;
import com.datafactory.task.domain.vo.TaskEnvironmentVO;
import com.datafactory.task.domain.vo.TaskVersionCompareVO;
import com.datafactory.task.domain.vo.TaskVersionVO;
import com.datafactory.task.domain.vo.TaskVO;

import java.util.List;

public interface TaskService {

    Long create(TaskCreateDTO createDTO);

    void update(Long id, TaskUpdateDTO updateDTO);

    void delete(Long id, Long updatedBy);

    TaskVO getDetail(Long id);

    TaskVO getByCode(String taskCode);

    PageResult<TaskVO> page(TaskPageQueryDTO queryDTO);

    List<TaskVersionVO> listVersions(Long taskId, String env);

    TaskVersionVO getVersionDetail(Long taskId, Long versionId);

    Long createVersion(Long taskId, TaskVersionSaveDTO saveDTO);

    void updateDag(Long taskId, Long versionId, TaskDagSaveDTO saveDTO);

    void deleteVersion(Long taskId, Long versionId);

    void publish(Long taskId, Long versionId, TaskVersionPublishDTO publishDTO);

    Long rollback(Long taskId, Long versionId, TaskVersionRollbackDTO rollbackDTO);

    Long rollbackEnvironment(Long taskId, TaskEnvironmentRollbackDTO rollbackDTO);

    void disable(Long taskId, Long operatorId);

    void updateStatus(Long taskId, TaskStatusUpdateDTO statusDTO);

    List<TaskVO> listEnabled();

    List<TaskEnvironmentVO> listEnvironments(Long taskId);

    Long promoteVersion(Long taskId, Long versionId, TaskVersionPromoteDTO promoteDTO);

    void updateVersionTestStatus(Long taskId, Long versionId, TaskVersionTestStatusUpdateDTO statusDTO);

    TaskAggregationVO getAggregation(Long taskId, String env);

    TaskVersionCompareVO compareVersions(Long taskId, Long sourceVersionId, Long targetVersionId);
}
