package com.datafactory.executor.service;

import com.datafactory.executor.domain.entity.TaskAggregation;

public interface TaskConfigService {

    TaskAggregation getTaskAggregation(Long taskId, String environment);
}
