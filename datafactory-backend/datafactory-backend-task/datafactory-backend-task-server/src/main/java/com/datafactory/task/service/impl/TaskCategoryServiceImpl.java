package com.datafactory.task.service.impl;

import com.datafactory.common.exception.BizException;
import com.datafactory.task.domain.dto.TaskCategoryCreateDTO;
import com.datafactory.task.domain.entity.TaskCategory;
import com.datafactory.task.domain.vo.TaskCategoryVO;
import com.datafactory.task.mapper.TaskCategoryMapper;
import com.datafactory.task.service.TaskCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskCategoryServiceImpl implements TaskCategoryService {

    private static final int BIZ_ERROR_CODE = 400;

    private final TaskCategoryMapper taskCategoryMapper;

    public TaskCategoryServiceImpl(TaskCategoryMapper taskCategoryMapper) {
        this.taskCategoryMapper = taskCategoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TaskCategoryCreateDTO createDTO) {
        if (!StringUtils.hasText(createDTO.getCategoryName())) {
            throw new BizException(BIZ_ERROR_CODE, "分类名称不能为空");
        }
        if (taskCategoryMapper.selectByCategoryName(createDTO.getCategoryName(), createDTO.getParentId()) != null) {
            throw new BizException(BIZ_ERROR_CODE, "同级分类名称已存在");
        }

        TaskCategory category = new TaskCategory();
        category.setParentId(createDTO.getParentId());
        category.setCategoryName(createDTO.getCategoryName());
        category.setSortNo(createDTO.getSortNo() == null ? 0 : createDTO.getSortNo());
        category.setCreatedBy(createDTO.getCreatedBy());
        category.setUpdatedBy(createDTO.getCreatedBy());
        taskCategoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public List<TaskCategoryVO> tree() {
        List<TaskCategory> categories = taskCategoryMapper.selectAll();
        Map<Long, TaskCategoryVO> categoryMap = new LinkedHashMap<>();
        List<TaskCategoryVO> roots = new ArrayList<>();

        for (TaskCategory category : categories) {
            categoryMap.put(category.getId(), toVO(category));
        }
        for (TaskCategory category : categories) {
            TaskCategoryVO vo = categoryMap.get(category.getId());
            if (category.getParentId() == null) {
                roots.add(vo);
                continue;
            }
            TaskCategoryVO parent = categoryMap.get(category.getParentId());
            if (parent == null) {
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        return roots;
    }

    private TaskCategoryVO toVO(TaskCategory category) {
        TaskCategoryVO vo = new TaskCategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setCategoryName(category.getCategoryName());
        vo.setLabel(category.getCategoryName());
        vo.setSortNo(category.getSortNo());
        return vo;
    }
}
