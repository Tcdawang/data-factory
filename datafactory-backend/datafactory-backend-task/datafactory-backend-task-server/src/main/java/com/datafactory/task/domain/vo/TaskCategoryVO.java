package com.datafactory.task.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskCategoryVO {

    private Long id;

    private Long parentId;

    private String categoryName;

    private String label;

    private Integer sortNo;

    private List<TaskCategoryVO> children = new ArrayList<>();
}
