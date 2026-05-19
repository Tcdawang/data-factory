package com.datafactory.script.mapper;

import com.datafactory.script.domain.entity.ComponentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ComponentInfoMapper {

    int insert(ComponentInfo componentInfo);

    int updateById(ComponentInfo componentInfo);

    int deleteById(@Param("id") Long id);

    ComponentInfo selectById(@Param("id") Long id);

    List<ComponentInfo> selectList(@Param("name") String name,
                                   @Param("type") String type,
                                   @Param("subType") String subType,
                                   @Param("status") String status);

    List<ComponentInfo> selectPage(@Param("name") String name,
                                   @Param("type") String type,
                                   @Param("subType") String subType,
                                   @Param("status") String status,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    int selectCount(@Param("name") String name,
                    @Param("type") String type,
                    @Param("subType") String subType,
                    @Param("status") String status);
}
