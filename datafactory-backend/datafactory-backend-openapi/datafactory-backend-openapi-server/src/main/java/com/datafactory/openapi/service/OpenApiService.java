package com.datafactory.openapi.service;

import com.datafactory.openapi.domain.CallResultVO;
import com.datafactory.openapi.domain.OpenApi;
import com.datafactory.openapi.domain.OpenApiDTO;

import java.util.List;
import java.util.Map;

public interface OpenApiService {
    Long create(OpenApiDTO dto);

    void update(Long id, OpenApiDTO dto);

    void delete(Long id, Long operatorId);

    void enable(Long id, Long operatorId);

    void disable(Long id, Long operatorId);

    OpenApi get(Long id);

    List<OpenApi> list();

    CallResultVO call(String apiPath, String apiKey, Map<String, Object> requestBody);
}
