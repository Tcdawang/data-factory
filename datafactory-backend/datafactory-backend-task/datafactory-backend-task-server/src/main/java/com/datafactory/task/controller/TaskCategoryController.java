package com.datafactory.task.controller;

import com.datafactory.common.result.Result;
import com.datafactory.task.domain.dto.TaskCategoryCreateDTO;
import com.datafactory.task.domain.vo.TaskCategoryVO;
import com.datafactory.task.service.TaskCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/tree")
    public Result<List<TaskCategoryVO>> tree() {
        return Result.success(taskCategoryService.tree());
    }
}
