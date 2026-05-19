package com.datafactory.script.service.impl;

import com.datafactory.common.exception.BizException;
import com.datafactory.common.result.PageResult;
import com.datafactory.script.domain.dto.ComponentCreateDTO;
import com.datafactory.script.domain.dto.ComponentPageQueryDTO;
import com.datafactory.script.domain.dto.ComponentTestDTO;
import com.datafactory.script.domain.dto.ComponentUpdateDTO;
import com.datafactory.script.domain.entity.ComponentInfo;
import com.datafactory.script.domain.entity.ComponentParam;
import com.datafactory.script.domain.vo.ComponentDetailVO;
import com.datafactory.script.domain.vo.ComponentParamVO;
import com.datafactory.script.domain.vo.ComponentTestResultVO;
import com.datafactory.script.domain.vo.ComponentVO;
import com.datafactory.script.mapper.ComponentInfoMapper;
import com.datafactory.script.mapper.ComponentParamMapper;
import com.datafactory.script.service.ComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComponentServiceImpl implements ComponentService {

    private final ComponentInfoMapper componentInfoMapper;
    private final ComponentParamMapper componentParamMapper;

    @Override
    @Transactional
    public Long create(ComponentCreateDTO createDTO) {
        ComponentInfo componentInfo = new ComponentInfo();
        BeanUtils.copyProperties(createDTO, componentInfo);
        componentInfo.setVersion("1.0.0");
        componentInfo.setStatus("active");
        componentInfoMapper.insert(componentInfo);
        return componentInfo.getId();
    }

    @Override
    @Transactional
    public void update(Long id, ComponentUpdateDTO updateDTO) {
        ComponentInfo componentInfo = componentInfoMapper.selectById(id);
        if (componentInfo == null) {
            throw new BizException(404, "组件不存在");
        }

        ComponentInfo updateInfo = new ComponentInfo();
        BeanUtils.copyProperties(updateDTO, updateInfo);
        updateInfo.setId(id);
        componentInfoMapper.updateById(updateInfo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ComponentInfo componentInfo = componentInfoMapper.selectById(id);
        if (componentInfo == null) {
            throw new BizException(404, "组件不存在");
        }

        componentInfoMapper.deleteById(id);
        componentParamMapper.deleteByComponentId(id);
    }

    @Override
    public ComponentDetailVO getDetail(Long id) {
        ComponentInfo componentInfo = componentInfoMapper.selectById(id);
        if (componentInfo == null) {
            throw new BizException(404, "组件不存在");
        }

        ComponentDetailVO detailVO = new ComponentDetailVO();
        BeanUtils.copyProperties(componentInfo, detailVO);

        List<ComponentParam> params = componentParamMapper.selectByComponentId(id);
        List<ComponentParamVO> paramVOs = params.stream()
                .map(this::convertToParamVO)
                .collect(Collectors.toList());
        detailVO.setParams(paramVOs);

        return detailVO;
    }

    @Override
    public PageResult<ComponentVO> page(ComponentPageQueryDTO queryDTO) {
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        List<ComponentInfo> list = componentInfoMapper.selectPage(
                queryDTO.getName(),
                queryDTO.getType(),
                queryDTO.getSubType(),
                queryDTO.getStatus(),
                offset,
                queryDTO.getPageSize()
        );
        Long total = (long) componentInfoMapper.selectCount(
                queryDTO.getName(),
                queryDTO.getType(),
                queryDTO.getSubType(),
                queryDTO.getStatus()
        );

        List<ComponentVO> voList = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public List<ComponentParamVO> listParams(Long componentId) {
        ComponentInfo componentInfo = componentInfoMapper.selectById(componentId);
        if (componentInfo == null) {
            throw new BizException(404, "组件不存在");
        }

        List<ComponentParam> params = componentParamMapper.selectByComponentId(componentId);
        return params.stream()
                .map(this::convertToParamVO)
                .collect(Collectors.toList());
    }

    @Override
    public ComponentTestResultVO testExecute(Long id, ComponentTestDTO testDTO) {
        ComponentInfo componentInfo = componentInfoMapper.selectById(id);
        if (componentInfo == null) {
            throw new BizException(404, "组件不存在");
        }

        // TODO: 调用组件执行器进行测试执行
        // 这里暂时返回模拟结果，实际实现需要根据组件类型调用对应的执行器
        ComponentTestResultVO resultVO = new ComponentTestResultVO();
        resultVO.setSuccess(true);
        resultVO.setMessage("测试执行成功");
        resultVO.setData(testDTO.getParams());
        resultVO.setExecutionTime(0L);

        return resultVO;
    }

    private ComponentVO convertToVO(ComponentInfo entity) {
        ComponentVO vo = new ComponentVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private ComponentParamVO convertToParamVO(ComponentParam entity) {
        ComponentParamVO vo = new ComponentParamVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
