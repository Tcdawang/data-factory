package com.datafactory.executor.feign;

import com.datafactory.common.result.Result;
import com.datafactory.executor.domain.entity.TaskAggregation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "datafactory-task", path = "/tasks")
public interface TaskServiceClient {

    @GetMapping("/{id}/aggregation")
    Result<TaskAggregation> getTaskAggregation(
            @PathVariable("id") Long taskId,
            @RequestParam("env") String environment);
}
