package com.datafactory.openapi.service.impl;

import com.datafactory.common.exception.BizException;
import com.datafactory.common.result.Result;
import com.datafactory.openapi.domain.CallResultVO;
import com.datafactory.openapi.domain.ExecutionRunVO;
import com.datafactory.openapi.domain.OpenApi;
import com.datafactory.openapi.domain.OpenApiCallLog;
import com.datafactory.openapi.domain.OpenApiDTO;
import com.datafactory.openapi.feign.ExecutorClient;
import com.datafactory.openapi.mapper.OpenApiCallLogMapper;
import com.datafactory.openapi.mapper.OpenApiMapper;
import com.datafactory.openapi.service.OpenApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class OpenApiServiceImpl implements OpenApiService {
    private static final int BIZ_ERROR_CODE = 400;
    private final OpenApiMapper openApiMapper;
    private final OpenApiCallLogMapper openApiCallLogMapper;
    private final ExecutorClient executorClient;
    private final ObjectMapper objectMapper;

    public OpenApiServiceImpl(OpenApiMapper openApiMapper,
                              OpenApiCallLogMapper openApiCallLogMapper,
                              ExecutorClient executorClient,
                              ObjectMapper objectMapper) {
        this.openApiMapper = openApiMapper;
        this.openApiCallLogMapper = openApiCallLogMapper;
        this.executorClient = executorClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Long create(OpenApiDTO dto) {
        validate(dto);
        OpenApi openApi = toEntity(null, dto);
        openApi.setCreatedBy(dto.getOperatorId());
        openApiMapper.insert(openApi);
        return openApi.getId();
    }

    @Override
    public void update(Long id, OpenApiDTO dto) {
        require(id);
        validate(dto);
        openApiMapper.update(toEntity(id, dto));
    }

    @Override
    public void delete(Long id, Long operatorId) {
        require(id);
        openApiMapper.delete(id, operatorId);
    }

    @Override
    public void enable(Long id, Long operatorId) {
        require(id);
        if (openApiMapper.updateStatus(id, "ENABLED", operatorId) == 0) {
            throw new BizException(BIZ_ERROR_CODE, "开放接口不存在或状态未更新");
        }
    }

    @Override
    public void disable(Long id, Long operatorId) {
        require(id);
        if (openApiMapper.updateStatus(id, "DISABLED", operatorId) == 0) {
            throw new BizException(BIZ_ERROR_CODE, "开放接口不存在或状态未更新");
        }
    }

    @Override
    public OpenApi get(Long id) {
        return require(id);
    }

    @Override
    public List<OpenApi> list() {
        return openApiMapper.list();
    }

    @Override
    public CallResultVO call(String apiPath, String apiKey, Map<String, Object> requestBody) {
        OpenApi openApi = openApiMapper.selectByPath(apiPath);
        if (openApi == null) {
            throw new BizException(BIZ_ERROR_CODE, "开放接口不存在");
        }
        if (!"ENABLED".equals(openApi.getStatus())) {
            throw new BizException(BIZ_ERROR_CODE, "开放接口未启用");
        }
        if (StringUtils.hasText(openApi.getApiKey()) && !openApi.getApiKey().equals(apiKey)) {
            throw new BizException(BIZ_ERROR_CODE, "API Key错误");
        }
        CallResultVO vo = new CallResultVO();
        vo.setTaskId(openApi.getTaskId());
        OpenApiCallLog callLog = new OpenApiCallLog();
        callLog.setOpenApiId(openApi.getId());
        callLog.setApiPath(openApi.getApiPath());
        callLog.setTaskId(openApi.getTaskId());
        callLog.setRequestJson(toJson(requestBody));
        try {
            Result<ExecutionRunVO> result = executorClient.runTask(openApi.getTaskId(), "PROD", "API", "openapi", requestBody == null ? Map.of() : requestBody);
            if (result == null || result.getCode() == null || result.getCode() != 0 || result.getData() == null) {
                throw new BizException(BIZ_ERROR_CODE, result == null ? "任务执行调用失败" : result.getMessage());
            }
            ExecutionRunVO execution = result.getData();
            vo.setExecutionId(execution.getExecutionId());
            vo.setStatus(execution.getStatus());
            callLog.setExecutionId(execution.getExecutionId());
            callLog.setStatus(execution.getStatus());
            callLog.setResponseJson(toJson(vo));
        } catch (Exception ex) {
            callLog.setStatus("FAILED");
            callLog.setErrorMessage(ex.getMessage());
            callLog.setResponseJson("{}");
            openApiCallLogMapper.insert(callLog);
            throw new BizException(BIZ_ERROR_CODE, "任务执行调用失败：" + ex.getMessage());
        }
        openApiCallLogMapper.insert(callLog);
        return vo;
    }

    private void validate(OpenApiDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getApiName())) {
            throw new BizException(BIZ_ERROR_CODE, "接口名称不能为空");
        }
        if (!StringUtils.hasText(dto.getApiPath())) {
            throw new BizException(BIZ_ERROR_CODE, "接口路径不能为空");
        }
        if (dto.getTaskId() == null) {
            throw new BizException(BIZ_ERROR_CODE, "绑定任务不能为空");
        }
    }

    private OpenApi require(Long id) {
        if (id == null) {
            throw new BizException(BIZ_ERROR_CODE, "接口ID不能为空");
        }
        OpenApi openApi = openApiMapper.selectById(id);
        if (openApi == null) {
            throw new BizException(BIZ_ERROR_CODE, "开放接口不存在");
        }
        return openApi;
    }

    private OpenApi toEntity(Long id, OpenApiDTO dto) {
        OpenApi openApi = new OpenApi();
        openApi.setId(id);
        openApi.setApiName(dto.getApiName());
        openApi.setApiPath(dto.getApiPath());
        openApi.setTaskId(dto.getTaskId());
        openApi.setApiKey(dto.getApiKey());
        openApi.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "DISABLED");
        openApi.setUpdatedBy(dto.getOperatorId());
        return openApi;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
