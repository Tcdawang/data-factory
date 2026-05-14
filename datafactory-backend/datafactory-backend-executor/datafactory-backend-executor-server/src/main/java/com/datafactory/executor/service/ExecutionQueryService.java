package com.datafactory.executor.service;

import com.datafactory.executor.domain.vo.ExecutionStatusVO;
import com.datafactory.executor.domain.vo.NodeExecutionLogVO;

import java.util.List;

public interface ExecutionQueryService {

    ExecutionStatusVO getExecutionStatus(String executionId);

    List<NodeExecutionLogVO> getExecutionLogs(String executionId);
}
