package com.datafactory.executor.feign;

import com.datafactory.common.result.Result;
import com.datafactory.executor.domain.entity.Datasource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "datafactory-datasource", path = "/datasources")
public interface DatasourceServiceClient {

    @GetMapping("/{id}")
    Result<Datasource> get(@PathVariable("id") Long id);
}
