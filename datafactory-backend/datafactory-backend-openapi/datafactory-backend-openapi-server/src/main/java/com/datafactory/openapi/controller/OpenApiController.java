package com.datafactory.openapi.controller;

import com.datafactory.common.result.Result;
import com.datafactory.common.result.PageResult;
import com.datafactory.openapi.domain.CallResultVO;
import com.datafactory.openapi.domain.OpenApi;
import com.datafactory.openapi.domain.OpenApiCallLogQueryDTO;
import com.datafactory.openapi.domain.OpenApiCallLogVO;
import com.datafactory.openapi.domain.OpenApiDTO;
import com.datafactory.openapi.service.OpenApiService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/openapi")
public class OpenApiController {
    private final OpenApiService openApiService;

    public OpenApiController(OpenApiService openApiService) {
        this.openApiService = openApiService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody OpenApiDTO dto) {
        return Result.success(openApiService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody OpenApiDTO dto) {
        openApiService.update(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id, @RequestParam(value = "operatorId", required = false) Long operatorId) {
        openApiService.delete(id, operatorId);
        return Result.success(null);
    }

    @PostMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable("id") Long id, @RequestParam(value = "operatorId", required = false) Long operatorId) {
        openApiService.enable(id, operatorId);
        return Result.success(null);
    }

    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable("id") Long id, @RequestParam(value = "operatorId", required = false) Long operatorId) {
        openApiService.disable(id, operatorId);
        return Result.success(null);
    }

    @GetMapping("/{id}")
    public Result<OpenApi> get(@PathVariable("id") Long id) {
        return Result.success(openApiService.get(id));
    }

    @GetMapping
    public Result<List<OpenApi>> list() {
        return Result.success(openApiService.list());
    }

    @GetMapping("/call-logs")
    public Result<PageResult<OpenApiCallLogVO>> pageCallLogs(OpenApiCallLogQueryDTO queryDTO) {
        return Result.success(openApiService.pageCallLogs(queryDTO));
    }

    @PostMapping("/call/{apiPath}")
    public Result<CallResultVO> call(@PathVariable("apiPath") String apiPath,
                                     @RequestHeader(value = "X-API-Key", required = false) String apiKey,
                                     @RequestBody(required = false) Map<String, Object> requestBody) {
        return Result.success(openApiService.call(apiPath, apiKey, requestBody));
    }

    @PostMapping("/call")
    public Result<CallResultVO> callByParam(@RequestParam("apiPath") String apiPath,
                                            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
                                            @RequestBody(required = false) Map<String, Object> requestBody) {
        return Result.success(openApiService.call(apiPath, apiKey, requestBody));
    }
}
