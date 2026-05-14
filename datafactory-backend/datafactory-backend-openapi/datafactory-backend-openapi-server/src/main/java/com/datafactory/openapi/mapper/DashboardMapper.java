package com.datafactory.openapi.mapper;

import com.datafactory.openapi.domain.RecentExecutionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DashboardMapper {
    Long countDatasources();

    Long countTasks();

    Long countOpenApis();

    Long countTodayExecutions();

    Long countLast24hExecutions();

    Long countLast24hSuccessExecutions();

    List<RecentExecutionVO> selectRecentExecutions(@Param("limit") int limit);
}
