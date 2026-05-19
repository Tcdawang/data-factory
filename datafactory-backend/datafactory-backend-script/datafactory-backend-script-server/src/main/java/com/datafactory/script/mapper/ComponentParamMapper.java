package com.datafactory.script.mapper;

import com.datafactory.script.domain.entity.ComponentParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ComponentParamMapper {

    int insert(ComponentParam componentParam);

    int batchInsert(@Param("list") List<ComponentParam> list);

    int updateById(ComponentParam componentParam);

    int deleteById(@Param("id") Long id);

    int deleteByComponentId(@Param("componentId") Long componentId);

    ComponentParam selectById(@Param("id") Long id);

    List<ComponentParam> selectByComponentId(@Param("componentId") Long componentId);
}
