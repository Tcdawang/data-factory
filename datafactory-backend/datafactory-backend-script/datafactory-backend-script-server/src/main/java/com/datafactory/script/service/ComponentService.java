package com.datafactory.script.service;

import com.datafactory.common.result.PageResult;
import com.datafactory.script.domain.dto.ComponentCreateDTO;
import com.datafactory.script.domain.dto.ComponentPageQueryDTO;
import com.datafactory.script.domain.dto.ComponentTestDTO;
import com.datafactory.script.domain.dto.ComponentUpdateDTO;
import com.datafactory.script.domain.vo.ComponentDetailVO;
import com.datafactory.script.domain.vo.ComponentParamVO;
import com.datafactory.script.domain.vo.ComponentTestResultVO;
import com.datafactory.script.domain.vo.ComponentVO;

import java.util.List;

public interface ComponentService {

    /**
     * 创建组件
     * @param createDTO 创建参数
     * @return 组件ID
     */
    Long create(ComponentCreateDTO createDTO);

    /**
     * 更新组件
     * @param id 组件ID
     * @param updateDTO 更新参数
     */
    void update(Long id, ComponentUpdateDTO updateDTO);

    /**
     * 删除组件
     * @param id 组件ID
     */
    void delete(Long id);

    /**
     * 获取组件详情
     * @param id 组件ID
     * @return 组件详情
     */
    ComponentDetailVO getDetail(Long id);

    /**
     * 分页查询组件列表
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<ComponentVO> page(ComponentPageQueryDTO queryDTO);

    /**
     * 获取组件参数列表
     * @param componentId 组件ID
     * @return 参数列表
     */
    List<ComponentParamVO> listParams(Long componentId);

    /**
     * 测试组件执行
     * @param id 组件ID
     * @param testDTO 测试参数
     * @return 执行结果
     */
    ComponentTestResultVO testExecute(Long id, ComponentTestDTO testDTO);
}
