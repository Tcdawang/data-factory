package com.datafactory.task.controller;

import com.datafactory.common.result.PageResult;
import com.datafactory.common.result.Result;
import com.datafactory.task.domain.dto.TaskCreateDTO;
import com.datafactory.task.domain.dto.TaskPageQueryDTO;
import com.datafactory.task.domain.dto.TaskUpdateDTO;
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

    @GetMapping("/{id}/versions/compare")
    public Result<TaskVersionCompareVO> compareVersions(
            @PathVariable("id") Long id,
            @RequestParam("sourceVersionId") Long sourceVersionId,
            @RequestParam("targetVersionId") Long targetVersionId) {
        return Result.success(taskService.compareVersions(id, sourceVersionId, targetVersionId));
    }
}
