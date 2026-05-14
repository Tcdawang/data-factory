package com.datafactory.task.controller;

import com.datafactory.common.result.Result;
import com.datafactory.task.domain.dto.TaskCategoryCreateDTO;
import com.datafactory.task.domain.dto.TaskCategoryUpdateDTO;
import com.datafactory.task.domain.vo.TaskCategoryVO;
import com.datafactory.task.service.TaskCategoryService;
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
@RequestMapping("/task-categories")
public class TaskCategoryController {

    private final TaskCategoryService taskCategoryService;

    public TaskCategoryController(TaskCategoryService taskCategoryService) {
        this.taskCategoryService = taskCategoryService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody TaskCategoryCreateDTO createDTO) {
        return Result.success(taskCategoryService.create(createDTO));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody TaskCategoryUpdateDTO updateDTO) {
        taskCategoryService.update(id, updateDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id,
                               @RequestParam(value = "updatedBy", required = false) Long updatedBy) {
        taskCategoryService.delete(id, updatedBy);
        return Result.success(null);
    }

    @GetMapping("/tree")
    public Result<List<TaskCategoryVO>> tree() {
        return Result.success(taskCategoryService.tree());
    }
}
