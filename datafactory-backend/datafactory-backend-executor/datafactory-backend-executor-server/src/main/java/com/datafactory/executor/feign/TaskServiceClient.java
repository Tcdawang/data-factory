package com.datafactory.executor.feign;

import com.datafactory.common.result.Result;
import com.datafactory.executor.domain.dto.TaskVersionTestStatusUpdateDTO;
import com.datafactory.executor.domain.entity.TaskAggregation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "datafactory-task", path = "/tasks")
public interface TaskServiceClient {

    @GetMapping("/{id}/aggregation")
    Result<TaskAggregation> getTaskAggregation(
            @PathVariable("id") Long taskId,
            @RequestParam("env") String environment);
    @PutMapping("/{id}/versions/{versionId}/test-status")
    Result<Void> updateVersionTestStatus(
            @PathVariable("id") Long taskId,
            @PathVariable("versionId") Long versionId,
            @RequestBody TaskVersionTestStatusUpdateDTO statusDTO);
}
