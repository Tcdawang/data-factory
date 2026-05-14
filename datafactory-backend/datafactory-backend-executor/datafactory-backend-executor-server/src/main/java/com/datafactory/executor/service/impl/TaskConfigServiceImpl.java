package com.datafactory.executor.service.impl;

import com.datafactory.common.exception.BizException;
import com.datafactory.executor.domain.entity.TaskAggregation;
import com.datafactory.executor.feign.TaskServiceClient;
import com.datafactory.executor.service.TaskConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TaskConfigServiceImpl implements TaskConfigService {

    private static final String CACHE_KEY_PREFIX = "task:aggregation:";
    private static final long CACHE_TTL_HOURS = 24;

    private final StringRedisTemplate redisTemplate;
    private final TaskServiceClient taskServiceClient;
    private final ObjectMapper objectMapper;

    public TaskConfigServiceImpl(StringRedisTemplate redisTemplate,
                                 TaskServiceClient taskServiceClient,
                                 ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.taskServiceClient = taskServiceClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public TaskAggregation getTaskAggregation(Long taskId, String environment) {
        TaskAggregation cached = loadFromCache(taskId, environment);
        if (cached != null) {
            return cached;
        }
        return loadFromRemote(taskId, environment);
    }

    private TaskAggregation loadFromCache(Long taskId, String environment) {
        String cacheKey = buildCacheKey(taskId, environment);
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (json != null) {
                return objectMapper.readValue(json, TaskAggregation.class);
            }
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存反序列化失败, key={}", cacheKey, e);
            redisTemplate.delete(cacheKey);
        }
        return null;
    }

    private TaskAggregation loadFromRemote(Long taskId, String environment) {
        var result = taskServiceClient.getTaskAggregation(taskId, environment);
        if (result == null || result.getCode() != 0 || result.getData() == null) {
            throw new BizException(400, "获取任务配置失败, taskId=" + taskId + ", env=" + environment);
        }
        TaskAggregation aggregation = result.getData();
        saveToCache(taskId, environment, aggregation);
        return aggregation;
    }

    private void saveToCache(Long taskId, String environment, TaskAggregation aggregation) {
        String cacheKey = buildCacheKey(taskId, environment);
        try {
            String json = objectMapper.writeValueAsString(aggregation);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存序列化失败, key={}", cacheKey, e);
        }
    }

    private String buildCacheKey(Long taskId, String environment) {
        return CACHE_KEY_PREFIX + taskId + ":" + environment;
    }
}
