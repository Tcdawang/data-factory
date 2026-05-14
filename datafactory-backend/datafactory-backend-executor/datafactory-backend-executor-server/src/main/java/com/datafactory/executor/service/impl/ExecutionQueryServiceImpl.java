package com.datafactory.executor.service.impl;

import com.datafactory.common.exception.BizException;
import com.datafactory.executor.domain.entity.NodeExecutionLog;
import com.datafactory.executor.domain.entity.TaskExecution;
import com.datafactory.executor.domain.vo.ExecutionStatusVO;
import com.datafactory.executor.domain.vo.NodeExecutionLogVO;
import com.datafactory.executor.mapper.NodeExecutionLogMapper;
import com.datafactory.executor.mapper.TaskExecutionMapper;
import com.datafactory.executor.service.ExecutionQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionQueryServiceImpl implements ExecutionQueryService {

    private static final int BIZ_ERROR_CODE = 400;

    private final TaskExecutionMapper taskExecutionMapper;
    private final NodeExecutionLogMapper nodeExecutionLogMapper;

    public ExecutionQueryServiceImpl(TaskExecutionMapper taskExecutionMapper,
                                     NodeExecutionLogMapper nodeExecutionLogMapper) {
        this.taskExecutionMapper = taskExecutionMapper;
        this.nodeExecutionLogMapper = nodeExecutionLogMapper;
    }

    @Override
    public ExecutionStatusVO getExecutionStatus(String executionId) {
        validateExecutionId(executionId);
        TaskExecution execution = taskExecutionMapper.selectByExecutionId(executionId);
        if (execution == null) {
            throw new BizException(BIZ_ERROR_CODE, "执行记录不存在, executionId=" + executionId);
        }
        return toStatusVO(execution);
    }

    @Override
    public List<NodeExecutionLogVO> getExecutionLogs(String executionId) {
        validateExecutionId(executionId);
        TaskExecution execution = taskExecutionMapper.selectByExecutionId(executionId);
        if (execution == null) {
            throw new BizException(BIZ_ERROR_CODE, "执行记录不存在, executionId=" + executionId);
        }
        List<NodeExecutionLog> logs = nodeExecutionLogMapper.selectByExecutionId(executionId);
        return logs.stream().map(this::toLogVO).toList();
    }

    private void validateExecutionId(String executionId) {
        if (executionId == null || executionId.isBlank()) {
            throw new BizException(BIZ_ERROR_CODE, "执行批次ID不能为空");
        }
    }

    private ExecutionStatusVO toStatusVO(TaskExecution execution) {
        ExecutionStatusVO vo = new ExecutionStatusVO();
        vo.setId(execution.getId());
        vo.setTaskId(execution.getTaskId());
        vo.setTaskCode(execution.getTaskCode());
        vo.setEnvironment(execution.getEnvironment());
        vo.setExecutionId(execution.getExecutionId());
        vo.setStatus(execution.getStatus());
        vo.setTriggerType(execution.getTriggerType());
        vo.setTriggerUser(execution.getTriggerUser());
        vo.setStartTime(execution.getStartTime());
        vo.setEndTime(execution.getEndTime());
        vo.setErrorMessage(execution.getErrorMessage());
        vo.setCreatedTime(execution.getCreatedTime());
        return vo;
    }

    private NodeExecutionLogVO toLogVO(NodeExecutionLog log) {
        NodeExecutionLogVO vo = new NodeExecutionLogVO();
        vo.setId(log.getId());
        vo.setExecutionId(log.getExecutionId());
        vo.setNodeId(log.getNodeId());
        vo.setNodeName(log.getNodeName());
        vo.setNodeType(log.getNodeType());
        vo.setStatus(log.getStatus());
        vo.setStartTime(log.getStartTime());
        vo.setEndTime(log.getEndTime());
        vo.setInputData(log.getInputData());
        vo.setOutputData(log.getOutputData());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setCreatedTime(log.getCreatedTime());
        return vo;
    }
}
