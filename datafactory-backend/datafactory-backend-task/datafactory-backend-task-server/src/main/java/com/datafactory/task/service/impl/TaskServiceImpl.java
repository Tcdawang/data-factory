package com.datafactory.task.service.impl;

import com.datafactory.common.exception.BizException;
import com.datafactory.common.result.PageResult;
import com.datafactory.task.domain.dto.TaskCreateDTO;
import com.datafactory.task.domain.dto.TaskPageQueryDTO;
import com.datafactory.task.domain.dto.TaskUpdateDTO;
import com.datafactory.task.domain.entity.Task;
import com.datafactory.task.domain.entity.TaskVersion;
import com.datafactory.task.domain.vo.TaskVersionCompareVO;
import com.datafactory.task.domain.vo.TaskVersionVO;
import com.datafactory.task.domain.vo.TaskVO;
import com.datafactory.task.mapper.TaskMapper;
import com.datafactory.task.mapper.TaskVersionMapper;
import com.datafactory.task.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
public class TaskServiceImpl implements TaskService {

    private static final int BIZ_ERROR_CODE = 400;
    private static final String DEFAULT_STATUS = "UNPUBLISHED";

    private final TaskMapper taskMapper;
    private final TaskVersionMapper taskVersionMapper;

    public TaskServiceImpl(TaskMapper taskMapper, TaskVersionMapper taskVersionMapper) {
        this.taskMapper = taskMapper;
        this.taskVersionMapper = taskVersionMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TaskCreateDTO createDTO) {
        validateRequired(createDTO.getTaskName(), "任务名称不能为空");
        validateRequired(createDTO.getTaskCode(), "任务编码不能为空");
        validateUniqueTaskName(createDTO.getTaskName(), null);
        validateUniqueTaskCode(createDTO.getTaskCode(), null);

        Task task = new Task();
        task.setTaskName(createDTO.getTaskName());
        task.setTaskCode(createDTO.getTaskCode());
        task.setCategoryId(createDTO.getCategoryId());
        task.setDescription(createDTO.getDescription());
        task.setStatus(StringUtils.hasText(createDTO.getStatus()) ? createDTO.getStatus() : DEFAULT_STATUS);
        task.setCreatedBy(createDTO.getCreatedBy());
        task.setUpdatedBy(createDTO.getCreatedBy());
        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TaskUpdateDTO updateDTO) {
        Task existing = requireTask(id);
        validateRequired(updateDTO.getTaskName(), "任务名称不能为空");
        validateRequired(updateDTO.getTaskCode(), "任务编码不能为空");
        validateUniqueTaskName(updateDTO.getTaskName(), existing.getId());
        validateUniqueTaskCode(updateDTO.getTaskCode(), existing.getId());

        Task task = new Task();
        task.setId(existing.getId());
        task.setTaskName(updateDTO.getTaskName());
        task.setTaskCode(updateDTO.getTaskCode());
        task.setCategoryId(updateDTO.getCategoryId());
        task.setDescription(updateDTO.getDescription());
        task.setStatus(StringUtils.hasText(updateDTO.getStatus()) ? updateDTO.getStatus() : DEFAULT_STATUS);
        task.setUpdatedBy(updateDTO.getUpdatedBy());
        taskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long updatedBy) {
        requireTask(id);
        taskMapper.deleteById(id, updatedBy);
    }

    @Override
    public TaskVO getDetail(Long id) {
        Task task = requireTask(id);
        TaskVO vo = toVO(task);
        if (task.getCurrentVersionId() != null) {
            TaskVersion version = taskVersionMapper.selectById(task.getCurrentVersionId());
            if (version != null) {
                vo.setLatestVersion(toVersionVO(version));
            }
        }
        return vo;
    }

    @Override
    public PageResult<TaskVO> page(TaskPageQueryDTO queryDTO) {
        normalizePage(queryDTO);
        long total = taskMapper.countPage(queryDTO);
        long offset = (long) (queryDTO.getPageNo() - 1) * queryDTO.getPageSize();
        List<TaskVO> records = taskMapper.selectPage(queryDTO, offset, queryDTO.getPageSize())
                .stream()
                .map(task -> {
                    TaskVO vo = toVO(task);
                    if (task.getCurrentVersionId() != null) {
                        TaskVersion version = taskVersionMapper.selectById(task.getCurrentVersionId());
                        if (version != null) {
                            vo.setLatestVersion(toVersionVO(version));
                        }
                    }
                    return vo;
                })
                .toList();
        return new PageResult<>(records, total, queryDTO.getPageNo(), queryDTO.getPageSize());
    }

    @Override
    public List<TaskVersionVO> listVersions(Long taskId, String env) {
        requireTask(taskId);
        List<TaskVersion> versions;
        if (StringUtils.hasText(env)) {
            versions = taskVersionMapper.selectByTaskIdAndEnv(taskId, env);
        } else {
            versions = taskVersionMapper.selectByTaskId(taskId);
        }
        return versions.stream().map(this::toVersionVO).toList();
    }

    @Override
    public TaskVersionCompareVO compareVersions(Long taskId, Long sourceVersionId, Long targetVersionId) {
        requireTask(taskId);
        if (sourceVersionId == null || targetVersionId == null) {
            throw new BizException(BIZ_ERROR_CODE, "源版本ID和目标版本ID不能为空");
        }
        if (sourceVersionId.equals(targetVersionId)) {
            throw new BizException(BIZ_ERROR_CODE, "源版本和目标版本不能相同");
        }

        TaskVersion sourceVersion = taskVersionMapper.selectById(sourceVersionId);
        if (sourceVersion == null || !sourceVersion.getTaskId().equals(taskId)) {
            throw new BizException(BIZ_ERROR_CODE, "源版本不存在或不属于该任务");
        }

        TaskVersion targetVersion = taskVersionMapper.selectById(targetVersionId);
        if (targetVersion == null || !targetVersion.getTaskId().equals(taskId)) {
            throw new BizException(BIZ_ERROR_CODE, "目标版本不存在或不属于该任务");
        }

        TaskVersionCompareVO compareVO = new TaskVersionCompareVO();
        compareVO.setSourceVersionId(sourceVersion.getId());
        compareVO.setSourceVersionNo(sourceVersion.getVersionNo());
        compareVO.setSourceEnv(sourceVersion.getEnv());
        compareVO.setSourceDagJson(sourceVersion.getDagJson());
        compareVO.setSourceDslJson(sourceVersion.getDslJson());
        compareVO.setSourceInputSchemaJson(sourceVersion.getInputSchemaJson());
        compareVO.setSourceOutputSchemaJson(sourceVersion.getOutputSchemaJson());

        compareVO.setTargetVersionId(targetVersion.getId());
        compareVO.setTargetVersionNo(targetVersion.getVersionNo());
        compareVO.setTargetEnv(targetVersion.getEnv());
        compareVO.setTargetDagJson(targetVersion.getDagJson());
        compareVO.setTargetDslJson(targetVersion.getDslJson());
        compareVO.setTargetInputSchemaJson(targetVersion.getInputSchemaJson());
        compareVO.setTargetOutputSchemaJson(targetVersion.getOutputSchemaJson());

        compareVO.setDagChanged(!Objects.equals(sourceVersion.getDagJson(), targetVersion.getDagJson()));
        compareVO.setDslChanged(!Objects.equals(sourceVersion.getDslJson(), targetVersion.getDslJson()));
        compareVO.setInputSchemaChanged(!Objects.equals(sourceVersion.getInputSchemaJson(), targetVersion.getInputSchemaJson()));
        compareVO.setOutputSchemaChanged(!Objects.equals(sourceVersion.getOutputSchemaJson(), targetVersion.getOutputSchemaJson()));

        return compareVO;
    }

    private Task requireTask(Long id) {
        if (id == null) {
            throw new BizException(BIZ_ERROR_CODE, "任务ID不能为空");
        }
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(BIZ_ERROR_CODE, "任务不存在");
        }
        return task;
    }

    private void validateRequired(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(BIZ_ERROR_CODE, message);
        }
    }

    private void validateUniqueTaskName(String taskName, Long excludeId) {
        if (taskMapper.selectByTaskName(taskName, excludeId) != null) {
            throw new BizException(BIZ_ERROR_CODE, "任务名称已存在");
        }
    }

    private void validateUniqueTaskCode(String taskCode, Long excludeId) {
        if (taskMapper.selectByTaskCode(taskCode, excludeId) != null) {
            throw new BizException(BIZ_ERROR_CODE, "任务编码已存在");
        }
    }

    private void normalizePage(TaskPageQueryDTO queryDTO) {
        if (queryDTO.getPageNo() == null || queryDTO.getPageNo() < 1) {
            queryDTO.setPageNo(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
    }

    private TaskVO toVO(Task task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setTaskName(task.getTaskName());
        vo.setTaskCode(task.getTaskCode());
        vo.setCategoryId(task.getCategoryId());
        vo.setDescription(task.getDescription());
        vo.setStatus(task.getStatus());
        vo.setCurrentVersionId(task.getCurrentVersionId());
        vo.setCreatedBy(task.getCreatedBy());
        vo.setUpdatedBy(task.getUpdatedBy());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setUpdatedAt(task.getUpdatedAt());
        return vo;
    }

    private TaskVersionVO toVersionVO(TaskVersion version) {
        TaskVersionVO vo = new TaskVersionVO();
        vo.setId(version.getId());
        vo.setTaskId(version.getTaskId());
        vo.setVersionNo(version.getVersionNo());
        vo.setEnv(version.getEnv());
        vo.setVersionStatus(version.getVersionStatus());
        vo.setPublishStatus(version.getPublishStatus());
        vo.setDagJson(version.getDagJson());
        vo.setDslJson(version.getDslJson());
        vo.setInputSchemaJson(version.getInputSchemaJson());
        vo.setOutputSchemaJson(version.getOutputSchemaJson());
        vo.setTestStatus(version.getTestStatus());
        vo.setTestExecutionId(version.getTestExecutionId());
        vo.setRollbackFromVersionId(version.getRollbackFromVersionId());
        vo.setPublishTime(version.getPublishTime());
        vo.setCreatedBy(version.getCreatedBy());
        vo.setCreatedAt(version.getCreatedAt());
        vo.setUpdatedAt(version.getUpdatedAt());
        return vo;
    }
}
