package com.datafactory.task.service.impl;

import com.datafactory.common.exception.BizException;
import com.datafactory.common.result.PageResult;
import com.datafactory.task.domain.dto.TaskCreateDTO;
import com.datafactory.task.domain.dto.TaskDagSaveDTO;
import com.datafactory.task.domain.dto.TaskEdgeDTO;
import com.datafactory.task.domain.dto.TaskNodeDTO;
import com.datafactory.task.domain.dto.TaskPageQueryDTO;
import com.datafactory.task.domain.dto.TaskStatusUpdateDTO;
import com.datafactory.task.domain.dto.TaskUpdateDTO;
import com.datafactory.task.domain.dto.TaskVersionPromoteDTO;
import com.datafactory.task.domain.dto.TaskVersionPublishDTO;
import com.datafactory.task.domain.dto.TaskVersionRollbackDTO;
import com.datafactory.task.domain.dto.TaskVersionSaveDTO;
import com.datafactory.task.domain.dto.TaskVersionTestStatusUpdateDTO;
import com.datafactory.task.domain.dto.TaskEnvironmentRollbackDTO;
import com.datafactory.task.domain.entity.Task;
import com.datafactory.task.domain.entity.TaskEdge;
import com.datafactory.task.domain.entity.TaskNode;
import com.datafactory.task.domain.entity.TaskPublishRecord;
import com.datafactory.task.domain.entity.TaskVersion;
import com.datafactory.task.domain.vo.TaskAggregationVO;
import com.datafactory.task.domain.vo.TaskEnvironmentVO;
import com.datafactory.task.domain.vo.TaskVersionCompareVO;
import com.datafactory.task.domain.vo.TaskVersionVO;
import com.datafactory.task.domain.vo.TaskVO;
import com.datafactory.task.mapper.TaskEdgeMapper;
import com.datafactory.task.mapper.TaskMapper;
import com.datafactory.task.mapper.TaskNodeMapper;
import com.datafactory.task.mapper.TaskPublishRecordMapper;
import com.datafactory.task.mapper.TaskVersionMapper;
import com.datafactory.task.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class TaskServiceImpl implements TaskService {

    private static final int BIZ_ERROR_CODE = 400;
    private static final String DEFAULT_TASK_STATUS = "ENABLED";
    private static final String DEFAULT_ENV = "DEV";
    private static final String PUBLISH_STATUS_UNPUBLISHED = "UNPUBLISHED";
    private static final String PUBLISH_STATUS_PUBLISHED = "PUBLISHED";
    private static final String PUBLISH_STATUS_DISABLED = "DISABLED";
    private static final String VERSION_STATUS_DRAFT = "DRAFT";
    private static final String VERSION_STATUS_PUBLISHED = "PUBLISHED";
    private static final String VERSION_STATUS_ARCHIVED = "ARCHIVED";
    private static final String TEST_STATUS_PASSED = "PASSED";
    private static final String TEST_STATUS_FAILED = "FAILED";
    private static final int VERSION_NO_MAX_LENGTH = 50;
    private static final List<String> ENVIRONMENTS = List.of("DEV", "TEST", "PROD");

    private final TaskMapper taskMapper;
    private final TaskVersionMapper taskVersionMapper;
    private final TaskNodeMapper taskNodeMapper;
    private final TaskEdgeMapper taskEdgeMapper;
    private final TaskPublishRecordMapper taskPublishRecordMapper;

    public TaskServiceImpl(TaskMapper taskMapper,
                           TaskVersionMapper taskVersionMapper,
                           TaskNodeMapper taskNodeMapper,
                           TaskEdgeMapper taskEdgeMapper,
                           TaskPublishRecordMapper taskPublishRecordMapper) {
        this.taskMapper = taskMapper;
        this.taskVersionMapper = taskVersionMapper;
        this.taskNodeMapper = taskNodeMapper;
        this.taskEdgeMapper = taskEdgeMapper;
        this.taskPublishRecordMapper = taskPublishRecordMapper;
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
        task.setStatus(StringUtils.hasText(createDTO.getStatus()) ? createDTO.getStatus() : DEFAULT_TASK_STATUS);
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
        task.setStatus(StringUtils.hasText(updateDTO.getStatus()) ? updateDTO.getStatus() : existing.getStatus());
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
    public TaskVO getByCode(String taskCode) {
        validateRequired(taskCode, "任务编码不能为空");
        Task task = taskMapper.selectByTaskCode(taskCode, null);
        if (task == null) {
            throw new BizException(BIZ_ERROR_CODE, "任务不存在");
        }
        return toVO(task);
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
    public TaskVersionVO getVersionDetail(Long taskId, Long versionId) {
        return toVersionVO(requireVersion(taskId, versionId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVersion(Long taskId, Long versionId) {
        TaskVersion version = requireVersion(taskId, versionId);
        ensureEditableVersion(version);
        taskNodeMapper.deleteByVersionId(versionId);
        taskEdgeMapper.deleteByVersionId(versionId);
        taskVersionMapper.logicDelete(versionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVersion(Long taskId, TaskVersionSaveDTO saveDTO) {
        requireTask(taskId);
        validateRequired(saveDTO.getVersionNo(), "版本号不能为空");
        validateVersionNoLength(saveDTO.getVersionNo());
        String env = normalizeEnv(StringUtils.hasText(saveDTO.getEnv()) ? saveDTO.getEnv() : DEFAULT_ENV);

        TaskVersion version = new TaskVersion();
        version.setTaskId(taskId);
        version.setVersionNo(saveDTO.getVersionNo());
        version.setEnv(env);
        version.setVersionStatus(VERSION_STATUS_DRAFT);
        version.setPublishStatus(PUBLISH_STATUS_UNPUBLISHED);
        version.setDagJson(defaultJson(saveDTO.getDagJson()));
        version.setDslJson(defaultJson(saveDTO.getDslJson()));
        version.setInputSchemaJson(saveDTO.getInputSchemaJson());
        version.setOutputSchemaJson(saveDTO.getOutputSchemaJson());
        version.setTestStatus("UNTESTED");
        version.setChangeLog(saveDTO.getChangeLog());
        version.setIsCurrent(0);
        version.setEnvStatus(1);
        version.setCreatedBy(saveDTO.getCreatedBy());
        taskVersionMapper.insert(version);
        return version.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDag(Long taskId, Long versionId, TaskDagSaveDTO saveDTO) {
        TaskVersion version = requireVersion(taskId, versionId);
        ensureEditableVersion(version);
        version.setDagJson(defaultJson(saveDTO.getDagJson()));
        version.setDslJson(defaultJson(saveDTO.getDslJson()));
        version.setInputSchemaJson(saveDTO.getInputSchemaJson());
        version.setOutputSchemaJson(saveDTO.getOutputSchemaJson());
        taskVersionMapper.updateDag(version);

        taskNodeMapper.deleteByVersionId(versionId);
        taskEdgeMapper.deleteByVersionId(versionId);
        List<TaskNode> nodes = saveDTO.getNodes() == null ? List.of() : saveDTO.getNodes().stream()
                .map(node -> toNode(taskId, versionId, node))
                .toList();
        if (!nodes.isEmpty()) {
            taskNodeMapper.insertBatch(nodes);
        }
        List<TaskEdge> edges = saveDTO.getEdges() == null ? List.of() : saveDTO.getEdges().stream()
                .map(edge -> toEdge(taskId, versionId, edge))
                .toList();
        if (!edges.isEmpty()) {
            taskEdgeMapper.insertBatch(edges);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long taskId, Long versionId, TaskVersionPublishDTO publishDTO) {
        Task task = requireTask(taskId);
        TaskVersion version = requireVersion(taskId, versionId);
        if (PUBLISH_STATUS_PUBLISHED.equals(version.getPublishStatus())) {
            throw new BizException(BIZ_ERROR_CODE, "已发布版本不能重复发布");
        }
        String env = normalizeEnv(version.getEnv());
        Long operatorId = publishDTO == null ? null : publishDTO.getOperatorId();
        String changeLog = publishDTO == null ? null : publishDTO.getChangeLog();
        taskVersionMapper.clearCurrentByTaskIdAndEnv(taskId, env);
        taskVersionMapper.updatePublishStatus(versionId, VERSION_STATUS_PUBLISHED, PUBLISH_STATUS_PUBLISHED, changeLog);
        taskVersionMapper.setCurrent(versionId);
        taskMapper.updatePublishState(taskId, "ENABLED", versionId, operatorId);
        insertPublishRecord(taskId, versionId, env, env, "PUBLISH", task.getCurrentVersionId(), versionId, operatorId, changeLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long rollback(Long taskId, Long versionId, TaskVersionRollbackDTO rollbackDTO) {
        Task task = requireTask(taskId);
        if (rollbackDTO == null || rollbackDTO.getTargetVersionId() == null) {
            throw new BizException(BIZ_ERROR_CODE, "回滚目标版本不能为空");
        }
        validateRequired(rollbackDTO.getReason(), "回滚原因不能为空");
        TaskVersion currentVersion = requireVersion(taskId, versionId);
        TaskVersion targetVersion = requireVersion(taskId, rollbackDTO.getTargetVersionId());
        String env = normalizeEnv(StringUtils.hasText(rollbackDTO.getEnv()) ? rollbackDTO.getEnv() : targetVersion.getEnv());
        if (!env.equals(currentVersion.getEnv()) || !env.equals(targetVersion.getEnv())) {
            throw new BizException(BIZ_ERROR_CODE, "只能回滚同环境版本");
        }
        TaskVersion rollbackVersion = copyVersion(taskId, targetVersion, env,
                nextRollbackVersionNo(targetVersion), "回滚到版本" + targetVersion.getVersionNo() + "：" + rollbackDTO.getReason(), rollbackDTO.getOperatorId());
        rollbackVersion.setVersionStatus("DEV".equals(env) ? VERSION_STATUS_DRAFT : VERSION_STATUS_PUBLISHED);
        rollbackVersion.setPublishStatus("DEV".equals(env) ? PUBLISH_STATUS_UNPUBLISHED : PUBLISH_STATUS_PUBLISHED);
        rollbackVersion.setIsCurrent(1);
        taskVersionMapper.insert(rollbackVersion);
        copyVersionGraph(taskId, targetVersion.getId(), rollbackVersion.getId());
        taskVersionMapper.clearCurrentByTaskIdAndEnv(taskId, env);
        taskVersionMapper.setCurrent(rollbackVersion.getId());
        if (PUBLISH_STATUS_PUBLISHED.equals(rollbackVersion.getPublishStatus())) {
            taskVersionMapper.updatePublishStatus(rollbackVersion.getId(), rollbackVersion.getVersionStatus(), rollbackVersion.getPublishStatus(), rollbackVersion.getChangeLog());
        }
        taskMapper.updatePublishState(taskId, "ENABLED", rollbackVersion.getId(), rollbackDTO.getOperatorId());
        insertPublishRecord(taskId, rollbackVersion.getId(), env, env, "ROLLBACK",
                task.getCurrentVersionId(), rollbackVersion.getId(), rollbackDTO.getOperatorId(), rollbackVersion.getChangeLog());
        return rollbackVersion.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long rollbackEnvironment(Long taskId, TaskEnvironmentRollbackDTO rollbackDTO) {
        requireTask(taskId);
        if (rollbackDTO == null) {
            throw new BizException(BIZ_ERROR_CODE, "回退参数不能为空");
        }
        String sourceEnv = normalizeEnv(rollbackDTO.getSourceEnv());
        String targetEnv = normalizeEnv(rollbackDTO.getTargetEnv());
        if (!"TEST".equals(sourceEnv) || !"DEV".equals(targetEnv)) {
            throw new BizException(BIZ_ERROR_CODE, "仅支持TEST回退DEV");
        }
        validateRequired(rollbackDTO.getReason(), "回退原因不能为空");
        TaskVersion sourceVersion = requireCurrentVersion(taskId, sourceEnv);
        TaskVersion beforeVersion = taskVersionMapper.selectCurrentByTaskIdAndEnv(taskId, targetEnv);
        TaskVersion devVersion = copyVersion(taskId, sourceVersion, targetEnv,
                nextPromotedVersionNo(sourceVersion, targetEnv), rollbackDTO.getReason(), rollbackDTO.getOperatorId());
        devVersion.setVersionStatus(VERSION_STATUS_DRAFT);
        devVersion.setPublishStatus(PUBLISH_STATUS_UNPUBLISHED);
        devVersion.setIsCurrent(1);
        taskVersionMapper.insert(devVersion);
        copyVersionGraph(taskId, sourceVersion.getId(), devVersion.getId());
        taskVersionMapper.clearCurrentByTaskIdAndEnv(taskId, targetEnv);
        taskVersionMapper.setCurrent(devVersion.getId());
        insertPublishRecord(taskId, devVersion.getId(), sourceEnv, targetEnv, "REVERT",
                beforeVersion == null ? null : beforeVersion.getId(), devVersion.getId(), rollbackDTO.getOperatorId(), rollbackDTO.getReason());
        return devVersion.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long taskId, Long operatorId) {
        Task task = requireTask(taskId);
        if (task.getCurrentVersionId() != null) {
            taskVersionMapper.updatePublishStatus(task.getCurrentVersionId(), VERSION_STATUS_ARCHIVED, PUBLISH_STATUS_DISABLED, "任务停用");
        }
        taskMapper.updatePublishState(taskId, "DISABLED", task.getCurrentVersionId(), operatorId);
        insertPublishRecord(taskId, task.getCurrentVersionId(), null, DEFAULT_ENV, "DISABLE", task.getCurrentVersionId(), null, operatorId, "任务停用");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long taskId, TaskStatusUpdateDTO statusDTO) {
        requireTask(taskId);
        if (statusDTO == null || !StringUtils.hasText(statusDTO.getStatus())) {
            throw new BizException(BIZ_ERROR_CODE, "任务状态不能为空");
        }
        String status = statusDTO.getStatus().trim().toUpperCase(Locale.ROOT);
        if (!List.of("ENABLED", "DISABLED").contains(status)) {
            throw new BizException(BIZ_ERROR_CODE, "任务状态仅支持ENABLED/DISABLED");
        }
        taskMapper.updateStatus(taskId, status, statusDTO.getOperatorId());
    }

    @Override
    public List<TaskVO> listEnabled() {
        return taskMapper.selectEnabled().stream().map(this::toVO).toList();
    }

    @Override
    public List<TaskEnvironmentVO> listEnvironments(Long taskId) {
        requireTask(taskId);
        return ENVIRONMENTS.stream()
                .map(env -> {
                    TaskEnvironmentVO vo = new TaskEnvironmentVO();
                    vo.setEnv(env);
                    vo.setEnvName(envName(env));
                    TaskVersion current = taskVersionMapper.selectCurrentByTaskIdAndEnv(taskId, env);
                    TaskVersion latest = taskVersionMapper.selectLatestByTaskIdAndEnv(taskId, env);
                    vo.setCurrentVersion(current == null ? null : toVersionVO(current));
                    vo.setLatestVersion(latest == null ? null : toVersionVO(latest));
                    vo.setVersionCount(taskVersionMapper.countByTaskIdAndEnv(taskId, env));
                    return vo;
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long promoteVersion(Long taskId, Long versionId, TaskVersionPromoteDTO promoteDTO) {
        requireTask(taskId);
        if (promoteDTO == null) {
            throw new BizException(BIZ_ERROR_CODE, "晋升参数不能为空");
        }
        validateRequired(promoteDTO.getChangeLog(), "变更说明不能为空");
        TaskVersion sourceVersion = requireVersion(taskId, versionId);
        String sourceEnv = normalizeEnv(promoteDTO.getSourceEnv());
        String targetEnv = normalizeEnv(promoteDTO.getTargetEnv());
        if (!sourceEnv.equals(sourceVersion.getEnv())) {
            throw new BizException(BIZ_ERROR_CODE, "源环境与版本环境不一致");
        }
        if (!("DEV".equals(sourceEnv) && "TEST".equals(targetEnv)) && !("TEST".equals(sourceEnv) && "PROD".equals(targetEnv))) {
            throw new BizException(BIZ_ERROR_CODE, "仅支持DEV晋升TEST或TEST晋升PROD");
        }
        if (!PUBLISH_STATUS_PUBLISHED.equals(sourceVersion.getPublishStatus()) || !Integer.valueOf(1).equals(sourceVersion.getIsCurrent())) {
            throw new BizException(BIZ_ERROR_CODE, "只能晋升当前已发布版本");
        }
        if ("TEST".equals(sourceEnv) && "PROD".equals(targetEnv) && !TEST_STATUS_PASSED.equals(sourceVersion.getTestStatus())) {
            throw new BizException(BIZ_ERROR_CODE, "TEST验证通过后才能晋升PROD");
        }
        TaskVersion beforeVersion = taskVersionMapper.selectCurrentByTaskIdAndEnv(taskId, targetEnv);
        TaskVersion targetVersion = copyVersion(taskId, sourceVersion, targetEnv,
                nextPromotedVersionNo(sourceVersion, targetEnv), promoteDTO.getChangeLog(), promoteDTO.getOperatorId());
        targetVersion.setVersionStatus(VERSION_STATUS_PUBLISHED);
        targetVersion.setPublishStatus(PUBLISH_STATUS_PUBLISHED);
        targetVersion.setIsCurrent(1);
        taskVersionMapper.insert(targetVersion);
        copyVersionGraph(taskId, sourceVersion.getId(), targetVersion.getId());
        taskVersionMapper.clearCurrentByTaskIdAndEnv(taskId, targetEnv);
        taskVersionMapper.setCurrent(targetVersion.getId());
        taskVersionMapper.updatePublishStatus(targetVersion.getId(), VERSION_STATUS_PUBLISHED, PUBLISH_STATUS_PUBLISHED, promoteDTO.getChangeLog());
        taskMapper.updatePublishState(taskId, "ENABLED", targetVersion.getId(), promoteDTO.getOperatorId());
        insertPublishRecord(taskId, targetVersion.getId(), sourceEnv, targetEnv, "PROMOTE",
                beforeVersion == null ? null : beforeVersion.getId(), targetVersion.getId(), promoteDTO.getOperatorId(), promoteDTO.getChangeLog());
        return targetVersion.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVersionTestStatus(Long taskId, Long versionId, TaskVersionTestStatusUpdateDTO statusDTO) {
        requireVersion(taskId, versionId);
        if (statusDTO == null || !StringUtils.hasText(statusDTO.getTestStatus())) {
            throw new BizException(BIZ_ERROR_CODE, "测试状态不能为空");
        }
        String testStatus = statusDTO.getTestStatus().trim().toUpperCase(Locale.ROOT);
        if (!List.of(TEST_STATUS_PASSED, TEST_STATUS_FAILED).contains(testStatus)) {
            throw new BizException(BIZ_ERROR_CODE, "测试状态仅支持PASSED/FAILED");
        }
        taskVersionMapper.updateTestStatus(versionId, testStatus, statusDTO.getTestExecutionId());
    }

    @Override
    public TaskAggregationVO getAggregation(Long taskId, String env) {
        Task task = requireTask(taskId);
        String environment = normalizeEnv(StringUtils.hasText(env) ? env : DEFAULT_ENV);
        TaskVersion version = taskVersionMapper.selectCurrentByTaskIdAndEnv(taskId, environment);
        if (version == null || !version.getTaskId().equals(taskId)) {
            throw new BizException(BIZ_ERROR_CODE, "当前环境没有可执行版本");
        }
        TaskAggregationVO vo = new TaskAggregationVO();
        vo.setTaskId(task.getId());
        vo.setTaskCode(task.getTaskCode());
        vo.setTaskName(task.getTaskName());
        vo.setEnvironment(environment);
        vo.setVersionId(version.getId());
        vo.setVersion(version.getVersionNo());
        vo.setDslContent(version.getDslJson());
        vo.setDagJson(version.getDagJson());
        vo.setNodes(taskNodeMapper.selectByVersionId(version.getId()));
        vo.setEdges(taskEdgeMapper.selectByVersionId(version.getId()));
        return vo;
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

    private TaskVersion requireVersion(Long taskId, Long versionId) {
        if (versionId == null) {
            throw new BizException(BIZ_ERROR_CODE, "版本ID不能为空");
        }
        TaskVersion version = taskVersionMapper.selectById(versionId);
        if (version == null || !version.getTaskId().equals(taskId)) {
            throw new BizException(BIZ_ERROR_CODE, "版本不存在或不属于该任务");
        }
        return version;
    }

    private TaskVersion requireCurrentVersion(Long taskId, String env) {
        TaskVersion version = taskVersionMapper.selectCurrentByTaskIdAndEnv(taskId, env);
        if (version == null) {
            throw new BizException(BIZ_ERROR_CODE, env + "环境当前版本不存在");
        }
        return version;
    }

    private void ensureEditableVersion(TaskVersion version) {
        if (!PUBLISH_STATUS_UNPUBLISHED.equals(version.getPublishStatus())) {
            throw new BizException(BIZ_ERROR_CODE, "仅未发布版本允许编辑或删除");
        }
    }

    private TaskVersion copyVersion(Long taskId, TaskVersion sourceVersion, String env, String versionNo, String changeLog, Long operatorId) {
        TaskVersion version = new TaskVersion();
        version.setTaskId(taskId);
        version.setVersionNo(versionNo);
        version.setEnv(env);
        version.setVersionStatus(sourceVersion.getVersionStatus());
        version.setPublishStatus(sourceVersion.getPublishStatus());
        version.setDagJson(defaultJson(sourceVersion.getDagJson()));
        version.setDslJson(defaultJson(sourceVersion.getDslJson()));
        version.setInputSchemaJson(sourceVersion.getInputSchemaJson());
        version.setOutputSchemaJson(sourceVersion.getOutputSchemaJson());
        version.setTestStatus(sourceVersion.getTestStatus());
        version.setTestExecutionId(sourceVersion.getTestExecutionId());
        version.setRollbackFromVersionId(sourceVersion.getId());
        version.setChangeLog(changeLog);
        version.setIsCurrent(0);
        version.setEnvStatus(1);
        version.setCreatedBy(operatorId);
        return version;
    }

    private void copyVersionGraph(Long taskId, Long sourceVersionId, Long targetVersionId) {
        List<TaskNode> nodes = taskNodeMapper.selectByVersionId(sourceVersionId)
                .stream()
                .map(node -> copyNode(taskId, targetVersionId, node))
                .toList();
        if (!nodes.isEmpty()) {
            taskNodeMapper.insertBatch(nodes);
        }
        List<TaskEdge> edges = taskEdgeMapper.selectByVersionId(sourceVersionId)
                .stream()
                .map(edge -> copyEdge(taskId, targetVersionId, edge))
                .toList();
        if (!edges.isEmpty()) {
            taskEdgeMapper.insertBatch(edges);
        }
    }


    private String normalizeEnv(String env) {
        if (!StringUtils.hasText(env)) {
            throw new BizException(BIZ_ERROR_CODE, "环境不能为空");
        }
        String normalizedEnv = env.trim().toUpperCase(Locale.ROOT);
        if (!ENVIRONMENTS.contains(normalizedEnv)) {
            throw new BizException(BIZ_ERROR_CODE, "环境仅支持DEV/TEST/PROD");
        }
        return normalizedEnv;
    }

    private String envName(String env) {
        return switch (env) {
            case "DEV" -> "开发环境";
            case "TEST" -> "测试环境";
            case "PROD" -> "生产环境";
            default -> env;
        };
    }

    private String defaultJson(String json) {
        return StringUtils.hasText(json) ? json : "{}";
    }

    private String nextRollbackVersionNo(TaskVersion targetVersion) {
        String suffix = "-rollback-" + System.currentTimeMillis();
        String baseVersionNo = originalVersionNo(targetVersion.getVersionNo());
        int maxBaseLength = VERSION_NO_MAX_LENGTH - suffix.length();
        if (maxBaseLength < 1) {
            throw new BizException(BIZ_ERROR_CODE, "版本号生成失败");
        }
        if (baseVersionNo.length() > maxBaseLength) {
            baseVersionNo = baseVersionNo.substring(0, maxBaseLength);
        }
        return baseVersionNo + suffix;
    }

    private String nextPromotedVersionNo(TaskVersion sourceVersion, String targetEnv) {
        String suffix = "-" + targetEnv.toLowerCase(Locale.ROOT) + "-" + System.currentTimeMillis();
        String baseVersionNo = originalVersionNo(sourceVersion.getVersionNo());
        int maxBaseLength = VERSION_NO_MAX_LENGTH - suffix.length();
        if (maxBaseLength < 1) {
            throw new BizException(BIZ_ERROR_CODE, "版本号生成失败");
        }
        if (baseVersionNo.length() > maxBaseLength) {
            baseVersionNo = baseVersionNo.substring(0, maxBaseLength);
        }
        return baseVersionNo + suffix;
    }

    private String originalVersionNo(String versionNo) {
        if (!StringUtils.hasText(versionNo)) {
            return "v";
        }
        return versionNo.replaceFirst("-(test|prod)-\\d{13}$", "");
    }

    private void validateVersionNoLength(String versionNo) {
        if (versionNo.length() > VERSION_NO_MAX_LENGTH) {
            throw new BizException(BIZ_ERROR_CODE, "版本号长度不能超过" + VERSION_NO_MAX_LENGTH + "个字符");
        }
    }

    private TaskNode copyNode(Long taskId, Long targetVersionId, TaskNode source) {
        TaskNode node = new TaskNode();
        node.setTaskId(taskId);
        node.setTaskVersionId(targetVersionId);
        node.setNodeKey(source.getNodeKey());
        node.setNodeName(source.getNodeName());
        node.setNodeType(source.getNodeType());
        node.setComponentId(source.getComponentId());
        node.setRefResourceId(source.getRefResourceId());
        node.setRefResourceType(source.getRefResourceType());
        node.setConfigJson(source.getConfigJson());
        node.setInputMappingJson(source.getInputMappingJson());
        node.setOutputSchemaJson(source.getOutputSchemaJson());
        node.setPositionX(source.getPositionX());
        node.setPositionY(source.getPositionY());
        return node;
    }

    private TaskEdge copyEdge(Long taskId, Long targetVersionId, TaskEdge source) {
        TaskEdge edge = new TaskEdge();
        edge.setTaskId(taskId);
        edge.setTaskVersionId(targetVersionId);
        edge.setSourceNodeKey(source.getSourceNodeKey());
        edge.setTargetNodeKey(source.getTargetNodeKey());
        edge.setConditionExpr(source.getConditionExpr());
        edge.setSortOrder(source.getSortOrder());
        return edge;
    }

    private TaskNode toNode(Long taskId, Long versionId, TaskNodeDTO dto) {
        validateRequired(dto.getNodeKey(), "节点Key不能为空");
        validateRequired(dto.getNodeName(), "节点名称不能为空");
        validateRequired(dto.getNodeType(), "节点类型不能为空");
        TaskNode node = new TaskNode();
        node.setTaskId(taskId);
        node.setTaskVersionId(versionId);
        node.setNodeKey(dto.getNodeKey());
        node.setNodeName(dto.getNodeName());
        node.setNodeType(dto.getNodeType());
        node.setComponentId(dto.getComponentId());
        node.setRefResourceId(dto.getRefResourceId());
        node.setRefResourceType(dto.getRefResourceType());
        node.setConfigJson(dto.getConfigJson());
        node.setInputMappingJson(dto.getInputMappingJson());
        node.setOutputSchemaJson(dto.getOutputSchemaJson());
        node.setPositionX(dto.getPositionX() == null ? 0 : dto.getPositionX());
        node.setPositionY(dto.getPositionY() == null ? 0 : dto.getPositionY());
        return node;
    }

    private TaskEdge toEdge(Long taskId, Long versionId, TaskEdgeDTO dto) {
        validateRequired(dto.getSourceNodeKey(), "上游节点不能为空");
        validateRequired(dto.getTargetNodeKey(), "下游节点不能为空");
        TaskEdge edge = new TaskEdge();
        edge.setTaskId(taskId);
        edge.setTaskVersionId(versionId);
        edge.setSourceNodeKey(dto.getSourceNodeKey());
        edge.setTargetNodeKey(dto.getTargetNodeKey());
        edge.setConditionExpr(dto.getConditionExpr());
        edge.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        return edge;
    }

    private void insertPublishRecord(Long taskId, Long versionId, String env, String type,
                                     Long beforeVersionId, Long afterVersionId, Long operatorId) {
        insertPublishRecord(taskId, versionId, env, env == null ? DEFAULT_ENV : env, type, beforeVersionId, afterVersionId, operatorId, null);
    }

    private void insertPublishRecord(Long taskId, Long versionId, String sourceEnv, String targetEnv, String type,
                                     Long beforeVersionId, Long afterVersionId, Long operatorId) {
        insertPublishRecord(taskId, versionId, sourceEnv, targetEnv, type, beforeVersionId, afterVersionId, operatorId, null);
    }

    private void insertPublishRecord(Long taskId, Long versionId, String sourceEnv, String targetEnv, String type,
                                     Long beforeVersionId, Long afterVersionId, Long operatorId, String remark) {
        TaskPublishRecord record = new TaskPublishRecord();
        record.setTaskId(taskId);
        record.setTaskVersionId(versionId);
        record.setSourceEnv(sourceEnv);
        record.setTargetEnv(targetEnv);
        record.setPublishType(type);
        record.setBeforeVersionId(beforeVersionId);
        record.setAfterVersionId(afterVersionId);
        record.setRemark(remark);
        record.setOperatorId(operatorId);
        taskPublishRecordMapper.insert(record);
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
