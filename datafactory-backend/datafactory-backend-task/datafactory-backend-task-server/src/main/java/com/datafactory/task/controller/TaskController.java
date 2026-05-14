package com.datafactory.task.controller;

import com.datafactory.common.result.PageResult;
import com.datafactory.common.result.Result;
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
import com.datafactory.task.service.TaskService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody TaskCreateDTO createDTO) {
        return Result.success(taskService.create(createDTO));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody TaskUpdateDTO updateDTO) {
        taskService.update(id, updateDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id, @RequestParam(value = "updatedBy", required = false) Long updatedBy) {
        taskService.delete(id, updatedBy);
        return Result.success(null);
    }

    @GetMapping("/code/{taskCode}")
    public Result<TaskVO> getByCode(@PathVariable("taskCode") String taskCode) {
        return Result.success(taskService.getByCode(taskCode));
    }

    @GetMapping("/enabled")
    public Result<List<TaskVO>> listEnabled() {
        return Result.success(taskService.listEnabled());
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestBody TaskStatusUpdateDTO statusDTO) {
        taskService.updateStatus(id, statusDTO);
        return Result.success(null);
    }

    @GetMapping("/{id}")
    public Result<TaskVO> getDetail(@PathVariable("id") Long id) {
        return Result.success(taskService.getDetail(id));
    }

    @GetMapping
    public Result<PageResult<TaskVO>> page(TaskPageQueryDTO queryDTO) {
        return Result.success(taskService.page(queryDTO));
    }

    @GetMapping("/{id}/versions")
    public Result<List<TaskVersionVO>> listVersions(
            @PathVariable("id") Long id,
            @RequestParam(value = "env", required = false) String env) {
        return Result.success(taskService.listVersions(id, env));
    }

    @GetMapping("/{id}/environments")
    public Result<List<TaskEnvironmentVO>> listEnvironments(@PathVariable("id") Long id) {
        return Result.success(taskService.listEnvironments(id));
    }

    @GetMapping("/{id}/versions/{versionId}")
    public Result<TaskVersionVO> getVersionDetail(@PathVariable("id") Long id,
                                                  @PathVariable("versionId") Long versionId) {
        return Result.success(taskService.getVersionDetail(id, versionId));
    }

    @DeleteMapping("/{id}/versions/{versionId}")
    public Result<Void> deleteVersion(@PathVariable("id") Long id,
                                      @PathVariable("versionId") Long versionId) {
        taskService.deleteVersion(id, versionId);
        return Result.success(null);
    }

    @PostMapping("/{id}/versions")
    public Result<Long> createVersion(@PathVariable("id") Long id, @RequestBody TaskVersionSaveDTO saveDTO) {
        return Result.success(taskService.createVersion(id, saveDTO));
    }

    @PutMapping("/{id}/versions/{versionId}/dag")
    public Result<Void> updateDag(@PathVariable("id") Long id,
                                  @PathVariable("versionId") Long versionId,
                                  @RequestBody TaskDagSaveDTO saveDTO) {
        taskService.updateDag(id, versionId, saveDTO);
        return Result.success(null);
    }

    @PostMapping("/{id}/versions/{versionId}/publish")
    public Result<Void> publish(@PathVariable("id") Long id,
                                @PathVariable("versionId") Long versionId,
                                @RequestBody TaskVersionPublishDTO publishDTO) {
        taskService.publish(id, versionId, publishDTO);
        return Result.success(null);
    }

    @PostMapping("/{id}/versions/{versionId}/rollback")
    public Result<Long> rollback(@PathVariable("id") Long id,
                                 @PathVariable("versionId") Long versionId,
                                 @RequestBody TaskVersionRollbackDTO rollbackDTO) {
        return Result.success(taskService.rollback(id, versionId, rollbackDTO));
    }

    @PostMapping("/{id}/rollback-env")
    public Result<Long> rollbackEnvironment(@PathVariable("id") Long id,
                                            @RequestBody TaskEnvironmentRollbackDTO rollbackDTO) {
        return Result.success(taskService.rollbackEnvironment(id, rollbackDTO));
    }

    @PutMapping("/{id}/versions/{versionId}/test-status")
    public Result<Void> updateVersionTestStatus(@PathVariable("id") Long id,
                                                @PathVariable("versionId") Long versionId,
                                                @RequestBody TaskVersionTestStatusUpdateDTO statusDTO) {
        taskService.updateVersionTestStatus(id, versionId, statusDTO);
        return Result.success(null);
    }

    @PostMapping("/{id}/versions/{versionId}/promote")
    public Result<Long> promote(@PathVariable("id") Long id,
                                @PathVariable("versionId") Long versionId,
                                @RequestBody TaskVersionPromoteDTO promoteDTO) {
        return Result.success(taskService.promoteVersion(id, versionId, promoteDTO));
    }

    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable("id") Long id,
                                @RequestParam(value = "operatorId", required = false) Long operatorId) {
        taskService.disable(id, operatorId);
        return Result.success(null);
    }

    @GetMapping("/{id}/aggregation")
    public Result<TaskAggregationVO> getAggregation(
            @PathVariable("id") Long id,
            @RequestParam(value = "env", defaultValue = "DEV") String env) {
        return Result.success(taskService.getAggregation(id, env));
    }

    @GetMapping("/{id}/versions/compare")
    public Result<TaskVersionCompareVO> compareVersions(
            @PathVariable("id") Long id,
            @RequestParam("sourceVersionId") Long sourceVersionId,
            @RequestParam("targetVersionId") Long targetVersionId) {
        return Result.success(taskService.compareVersions(id, sourceVersionId, targetVersionId));
    }
}
