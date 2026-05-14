package com.datafactory.datasource.service.impl;

import com.datafactory.common.exception.BizException;
import com.datafactory.common.result.PageResult;
import com.datafactory.datasource.domain.Datasource;
import com.datafactory.datasource.domain.DatasourceDTO;
import com.datafactory.datasource.domain.DatasourceQueryDTO;
import com.datafactory.datasource.domain.TestResultVO;
import com.datafactory.datasource.mapper.DatasourceMapper;
import com.datafactory.datasource.service.DatasourceService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DatasourceServiceImpl implements DatasourceService {
    private static final int BIZ_ERROR_CODE = 400;
    private static final String STATUS_UNPUBLISHED = "UNPUBLISHED";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final String TYPE_MYSQL = "MYSQL";
    private static final String TYPE_REDIS = "REDIS";
    private static final String TYPE_MONGODB = "MONGODB";
    private static final String TYPE_HTTP_API = "HTTP_API";
    private static final Set<String> SUPPORTED_TYPES = Set.of(TYPE_MYSQL, TYPE_REDIS, TYPE_MONGODB, TYPE_HTTP_API);
    private final DatasourceMapper datasourceMapper;

    public DatasourceServiceImpl(DatasourceMapper datasourceMapper) {
        this.datasourceMapper = datasourceMapper;
    }

    @Override
    public Long create(DatasourceDTO dto) {
        validate(dto);
        Datasource datasource = toEntity(null, dto);
        datasource.setCreatedBy(dto.getOperatorId());
        datasource.setStatus(STATUS_UNPUBLISHED);
        datasourceMapper.insert(datasource);
        return datasource.getId();
    }

    @Override
    public void update(Long id, DatasourceDTO dto) {
        require(id);
        validateForUpdate(id, dto);
        datasourceMapper.update(toEntity(id, dto));
    }

    @Override
    public void delete(Long id, Long operatorId) {
        require(id);
        datasourceMapper.delete(id, operatorId);
    }

    @Override
    public void publish(Long id, Long operatorId) {
        Datasource datasource = require(id);
        if (STATUS_PUBLISHED.equals(datasource.getStatus())) {
            return;
        }
        if (datasourceMapper.updateStatus(id, STATUS_PUBLISHED, operatorId) == 0) {
            throw new BizException(BIZ_ERROR_CODE, "数据源状态更新失败");
        }
    }

    @Override
    public void disable(Long id, Long operatorId) {
        Datasource datasource = require(id);
        if (!STATUS_PUBLISHED.equals(datasource.getStatus())) {
            throw new BizException(BIZ_ERROR_CODE, "只有已发布的数据源可以停用");
        }
        if (datasourceMapper.updateStatus(id, STATUS_DISABLED, operatorId) == 0) {
            throw new BizException(BIZ_ERROR_CODE, "数据源状态更新失败");
        }
    }

    @Override
    public Datasource get(Long id) {
        return require(id);
    }

    @Override
    public PageResult<Datasource> page(DatasourceQueryDTO queryDTO) {
        int pageNo = queryDTO.getPageNo() == null || queryDTO.getPageNo() < 1 ? 1 : queryDTO.getPageNo();
        int pageSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 ? 10 : queryDTO.getPageSize();
        long total = datasourceMapper.count(queryDTO.getDatasourceName(), queryDTO.getDatasourceType(), queryDTO.getStatus());
        List<Datasource> records = datasourceMapper.page(queryDTO.getDatasourceName(), queryDTO.getDatasourceType(), queryDTO.getStatus(), (long) (pageNo - 1) * pageSize, pageSize);
        return new PageResult<>(records, total, pageNo, pageSize);
    }

    @Override
    public TestResultVO test(Long id) {
        Datasource datasource = require(id);
        TestResultVO vo = new TestResultVO();
        try {
            switch (normalizeType(datasource.getDatasourceType())) {
                case TYPE_MYSQL -> testMysql(datasource);
                case TYPE_REDIS -> testRedis(datasource);
                case TYPE_MONGODB -> testMongoDb(datasource);
                case TYPE_HTTP_API -> testHttpApi(datasource);
                default -> throw new BizException(BIZ_ERROR_CODE, "数据源类型不支持");
            }
            vo.setSuccess(true);
            vo.setMessage("连接测试成功");
        } catch (Exception ex) {
            vo.setSuccess(false);
            vo.setMessage(ex.getMessage());
        }
        return vo;
    }

    private void testMysql(Datasource datasource) throws Exception {
        if (!StringUtils.hasText(datasource.getJdbcUrl())) {
            throw new BizException(BIZ_ERROR_CODE, "MySQL JDBC URL不能为空");
        }
        try (Connection ignored = DriverManager.getConnection(datasource.getJdbcUrl(), datasource.getUsername(), datasource.getPassword())) {
        }
    }

    private void testRedis(Datasource datasource) {
        if (!StringUtils.hasText(datasource.getJdbcUrl())) {
            throw new BizException(BIZ_ERROR_CODE, "Redis URI不能为空");
        }
        RedisClient client = RedisClient.create(datasource.getJdbcUrl());
        client.setDefaultTimeout(Duration.ofSeconds(5));
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            connection.sync().ping();
        } finally {
            client.shutdown();
        }
    }

    private void testMongoDb(Datasource datasource) {
        if (!StringUtils.hasText(datasource.getJdbcUrl())) {
            throw new BizException(BIZ_ERROR_CODE, "MongoDB URI不能为空");
        }
        try (MongoClient client = MongoClients.create(datasource.getJdbcUrl())) {
            client.getDatabase("admin").runCommand(new Document("ping", 1));
        }
    }

    private void testHttpApi(Datasource datasource) throws Exception {
        if (!StringUtils.hasText(datasource.getJdbcUrl())) {
            throw new BizException(BIZ_ERROR_CODE, "HTTP API基础地址不能为空");
        }
        HttpURLConnection connection = (HttpURLConnection) URI.create(datasource.getJdbcUrl()).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        int statusCode = connection.getResponseCode();
        if (statusCode >= 500) {
            throw new BizException(BIZ_ERROR_CODE, "HTTP API连接测试失败，状态码：" + statusCode);
        }
    }

    private void validate(DatasourceDTO dto) {
        validateCommon(dto);
        if (datasourceMapper.countByName(dto.getDatasourceName(), null) > 0) {
            throw new BizException(BIZ_ERROR_CODE, "数据源名称已存在");
        }
        if (datasourceMapper.countByJdbcUrl(dto.getJdbcUrl(), null) > 0) {
            throw new BizException(BIZ_ERROR_CODE, "连接信息已存在");
        }
    }

    private void validateForUpdate(Long id, DatasourceDTO dto) {
        validateCommon(dto);
        if (datasourceMapper.countByName(dto.getDatasourceName(), id) > 0) {
            throw new BizException(BIZ_ERROR_CODE, "数据源名称已存在");
        }
        if (datasourceMapper.countByJdbcUrl(dto.getJdbcUrl(), id) > 0) {
            throw new BizException(BIZ_ERROR_CODE, "连接信息已存在");
        }
    }

    private void validateCommon(DatasourceDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getDatasourceName())) {
            throw new BizException(BIZ_ERROR_CODE, "数据源名称不能为空");
        }
        if (!dto.getDatasourceName().matches("^[\\u4e00-\\u9fa5A-Za-z0-9_]+$")) {
            throw new BizException(BIZ_ERROR_CODE, "数据源名称只支持中英文、数字、下划线");
        }
        String datasourceType = normalizeType(dto.getDatasourceType());
        if (!SUPPORTED_TYPES.contains(datasourceType)) {
            throw new BizException(BIZ_ERROR_CODE, "数据源类型仅支持MYSQL/REDIS/MONGODB/HTTP_API");
        }
        if (!StringUtils.hasText(dto.getJdbcUrl())) {
            throw new BizException(BIZ_ERROR_CODE, "连接信息不能为空");
        }
    }

    private Datasource require(Long id) {
        if (id == null) {
            throw new BizException(BIZ_ERROR_CODE, "数据源ID不能为空");
        }
        Datasource datasource = datasourceMapper.selectById(id);
        if (datasource == null) {
            throw new BizException(BIZ_ERROR_CODE, "数据源不存在");
        }
        return datasource;
    }

    private Datasource toEntity(Long id, DatasourceDTO dto) {
        Datasource datasource = new Datasource();
        datasource.setId(id);
        datasource.setDatasourceName(dto.getDatasourceName());
        datasource.setDatasourceType(normalizeType(dto.getDatasourceType()));
        datasource.setDescription(dto.getDescription());
        datasource.setJdbcUrl(dto.getJdbcUrl());
        datasource.setUsername(dto.getUsername());
        datasource.setPassword(dto.getPassword());
        datasource.setUpdatedBy(dto.getOperatorId());
        return datasource;
    }

    private String normalizeType(String datasourceType) {
        return StringUtils.hasText(datasourceType) ? datasourceType.trim().toUpperCase(Locale.ROOT) : TYPE_MYSQL;
    }
}
