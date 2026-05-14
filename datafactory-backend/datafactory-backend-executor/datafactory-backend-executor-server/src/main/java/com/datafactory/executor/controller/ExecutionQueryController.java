package com.datafactory.executor.controller;

import com.datafactory.common.result.Result;
import com.datafactory.executor.domain.vo.ExecutionStatusVO;
import com.datafactory.executor.domain.vo.NodeExecutionLogVO;
import com.datafactory.executor.service.ExecutionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/datafactory-executor/execution")
public class ExecutionQueryController {

    private final ExecutionQueryService executionQueryService;

    public ExecutionQueryController(ExecutionQueryService executionQueryService) {
        this.executionQueryService = executionQueryService;
    }

    @GetMapping("/{executionId}/status")
    public Result<ExecutionStatusVO> getStatus(@PathVariable("executionId") String executionId) {
        return Result.success(executionQueryService.getExecutionStatus(executionId));
    }

    @GetMapping("/{executionId}/logs")
    public Result<List<NodeExecutionLogVO>> getLogs(@PathVariable("executionId") String executionId) {
        return Result.success(executionQueryService.getExecutionLogs(executionId));
    }
}
