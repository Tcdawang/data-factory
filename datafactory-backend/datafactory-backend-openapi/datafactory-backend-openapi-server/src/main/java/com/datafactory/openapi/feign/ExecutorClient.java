package com.datafactory.openapi.feign;

import com.datafactory.common.result.Result;
import com.datafactory.openapi.domain.ExecutionRunVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "datafactory-executor", path = "/datafactory-executor/execution")
public interface ExecutorClient {

    @PostMapping("/tasks/{taskId}/run")
    Result<ExecutionRunVO> runTask(@PathVariable("taskId") Long taskId,
                                   @RequestParam("env") String env,
                                   @RequestParam("triggerType") String triggerType,
                                   @RequestParam("triggerUser") String triggerUser,
                                   @RequestBody Map<String, Object> inputData);
}
