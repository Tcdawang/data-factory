package com.datafactory.executor.service;

import com.datafactory.executor.domain.vo.ExecutionRunVO;

import java.util.Map;

public interface ExecutionRunService {
    ExecutionRunVO runTask(Long taskId, String env, String triggerType, String triggerUser, Map<String, Object> inputData);
}
