package com.datafactory.executor.controller;

import com.datafactory.common.result.PageResult;
import com.datafactory.common.result.Result;
import com.datafactory.executor.domain.vo.ExecutionStatusVO;
import com.datafactory.executor.domain.vo.ExecutionRunVO;
import com.datafactory.executor.domain.vo.NodeExecutionLogVO;
import com.datafactory.executor.service.ExecutionQueryService;
import com.datafactory.executor.service.ExecutionRunService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/datafactory-executor/execution")
public class ExecutionQueryController {

    private final ExecutionQueryService executionQueryService;
    private final ExecutionRunService executionRunService;

    public ExecutionQueryController(ExecutionQueryService executionQueryService,
                                    ExecutionRunService executionRunService) {
        this.executionQueryService = executionQueryService;
        this.executionRunService = executionRunService;
    }

    @PostMapping("/tasks/{taskId}/run")
    public Result<ExecutionRunVO> runTask(@PathVariable("taskId") Long taskId,
                                          @RequestParam(value = "env", defaultValue = "DEV") String env,
                                          @RequestParam(value = "triggerType", defaultValue = "MANUAL") String triggerType,
                                          @RequestParam(value = "triggerUser", required = false) String triggerUser,
                                          @RequestBody(required = false) Map<String, Object> inputData) {
        return Result.success(executionRunService.runTask(taskId, env, triggerType, triggerUser, inputData));
    }

    @GetMapping("/page")
    public Result<PageResult<ExecutionStatusVO>> page(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return Result.success(executionQueryService.pageExecutions(pageNo, pageSize));
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
