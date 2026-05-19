package com.datafactory.script.controller;

import com.datafactory.common.result.PageResult;
import com.datafactory.common.result.Result;
import com.datafactory.script.domain.dto.ComponentCreateDTO;
import com.datafactory.script.domain.dto.ComponentPageQueryDTO;
import com.datafactory.script.domain.dto.ComponentTestDTO;
import com.datafactory.script.domain.dto.ComponentUpdateDTO;
import com.datafactory.script.domain.vo.ComponentDetailVO;
import com.datafactory.script.domain.vo.ComponentParamVO;
import com.datafactory.script.domain.vo.ComponentTestResultVO;
import com.datafactory.script.domain.vo.ComponentVO;
import com.datafactory.script.service.ComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;

    /**
     * 组件列表接口
     */
    @GetMapping
    public Result<PageResult<ComponentVO>> page(ComponentPageQueryDTO queryDTO) {
        return Result.success(componentService.page(queryDTO));
    }

    /**
     * 组件详情接口
     */
    @GetMapping("/{id}")
    public Result<ComponentDetailVO> getDetail(@PathVariable("id") Long id) {
        return Result.success(componentService.getDetail(id));
    }

    /**
     * 组件创建接口
     */
    @PostMapping
    public Result<Long> create(@RequestBody ComponentCreateDTO createDTO) {
        return Result.success(componentService.create(createDTO));
    }

    /**
     * 组件更新接口
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @RequestBody ComponentUpdateDTO updateDTO) {
        componentService.update(id, updateDTO);
        return Result.success(null);
    }

    /**
     * 组件删除接口
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        componentService.delete(id);
        return Result.success(null);
    }

    /**
     * 组件参数配置接口
     */
    @GetMapping("/{id}/params")
    public Result<List<ComponentParamVO>> listParams(@PathVariable("id") Long id) {
        return Result.success(componentService.listParams(id));
    }

    /**
     * 组件执行测试接口
     */
    @PostMapping("/{id}/test")
    public Result<ComponentTestResultVO> testExecute(@PathVariable("id") Long id,
                                                      @RequestBody ComponentTestDTO testDTO) {
        return Result.success(componentService.testExecute(id, testDTO));
    }
}
