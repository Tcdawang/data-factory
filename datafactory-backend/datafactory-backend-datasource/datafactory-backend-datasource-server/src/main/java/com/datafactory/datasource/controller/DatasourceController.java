package com.datafactory.datasource.controller;

import com.datafactory.common.result.PageResult;
import com.datafactory.common.result.Result;
import com.datafactory.datasource.domain.Datasource;
import com.datafactory.datasource.domain.DatasourceDTO;
import com.datafactory.datasource.domain.DatasourceQueryDTO;
import com.datafactory.datasource.domain.TestResultVO;
import com.datafactory.datasource.service.DatasourceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/datasources")
public class DatasourceController {
    private final DatasourceService datasourceService;

    public DatasourceController(DatasourceService datasourceService) {
        this.datasourceService = datasourceService;
    }

    @PostMapping
    public Result<Long> create(@RequestBody DatasourceDTO dto) {
        return Result.success(datasourceService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody DatasourceDTO dto) {
        datasourceService.update(id, dto);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id, @RequestParam(value = "operatorId", required = false) Long operatorId) {
        datasourceService.delete(id, operatorId);
        return Result.success(null);
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable("id") Long id, @RequestParam(value = "operatorId", required = false) Long operatorId) {
        datasourceService.publish(id, operatorId);
        return Result.success(null);
    }

    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable("id") Long id, @RequestParam(value = "operatorId", required = false) Long operatorId) {
        datasourceService.disable(id, operatorId);
        return Result.success(null);
    }

    @GetMapping("/{id}")
    public Result<Datasource> get(@PathVariable("id") Long id) {
        return Result.success(datasourceService.get(id));
    }

    @GetMapping
    public Result<PageResult<Datasource>> page(DatasourceQueryDTO queryDTO) {
        return Result.success(datasourceService.page(queryDTO));
    }

    @PostMapping("/{id}/test")
    public Result<TestResultVO> test(@PathVariable("id") Long id) {
        return Result.success(datasourceService.test(id));
    }
}
