package com.datafactory.executor.service.impl;

import com.datafactory.common.exception.BizException;
import com.datafactory.common.result.Result;
import com.datafactory.executor.domain.dto.TaskVersionTestStatusUpdateDTO;
import com.datafactory.executor.domain.entity.Datasource;
import com.datafactory.executor.domain.entity.NodeExecutionLog;
import com.datafactory.executor.domain.entity.TaskAggregation;
import com.datafactory.executor.domain.entity.TaskEdge;
import com.datafactory.executor.domain.entity.TaskExecution;
import com.datafactory.executor.domain.entity.TaskNode;
import com.datafactory.executor.domain.vo.ExecutionRunVO;
import com.datafactory.executor.feign.DatasourceServiceClient;
import com.datafactory.executor.feign.TaskServiceClient;
import com.datafactory.executor.mapper.NodeExecutionLogMapper;
import com.datafactory.executor.mapper.TaskExecutionMapper;
import com.datafactory.executor.service.ExecutionRunService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ExecutionRunServiceImpl implements ExecutionRunService {

    private static final int BIZ_ERROR_CODE = 400;
    private static final String REF_RESOURCE_TYPE_DATASOURCE = "DATASOURCE";
    private static final String DATASOURCE_TYPE_HTTP_API = "HTTP_API";
    private static final String DATASOURCE_TYPE_MYSQL = "MYSQL";
    private static final int DATASOURCE_LOAD_MAX_ATTEMPTS = 3;
    private static final Pattern WRITE_SQL_PATTERN = Pattern.compile("\\b(insert|update|delete|drop|alter|truncate|create|replace|merge|call|grant|revoke)\\b", Pattern.CASE_INSENSITIVE);

    private final TaskServiceClient taskServiceClient;
    private final DatasourceServiceClient datasourceServiceClient;
    private final TaskExecutionMapper taskExecutionMapper;
    private final NodeExecutionLogMapper nodeExecutionLogMapper;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public ExecutionRunServiceImpl(TaskServiceClient taskServiceClient,
                                   DatasourceServiceClient datasourceServiceClient,
                                   TaskExecutionMapper taskExecutionMapper,
                                   NodeExecutionLogMapper nodeExecutionLogMapper,
                                   ObjectMapper objectMapper) {
        this.taskServiceClient = taskServiceClient;
        this.datasourceServiceClient = datasourceServiceClient;
        this.taskExecutionMapper = taskExecutionMapper;
        this.nodeExecutionLogMapper = nodeExecutionLogMapper;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionRunVO runTask(Long taskId, String env, String triggerType, String triggerUser, Map<String, Object> inputData) {
        if (taskId == null) {
            throw new BizException(BIZ_ERROR_CODE, "任务ID不能为空");
        }
        String environment = StringUtils.hasText(env) ? env.trim().toUpperCase(Locale.ROOT) : "DEV";
        TaskAggregation aggregation = loadAggregation(taskId, environment);
        String executionId = UUID.randomUUID().toString().replace("-", "");

        TaskExecution execution = new TaskExecution();
        execution.setTaskId(aggregation.getTaskId());
        execution.setTaskCode(aggregation.getTaskCode());
        execution.setEnvironment(environment);
        execution.setExecutionId(executionId);
        execution.setStatus("RUNNING");
        execution.setTriggerType(StringUtils.hasText(triggerType) ? triggerType : "MANUAL");
        execution.setTriggerUser(triggerUser);
        execution.setStartTime(LocalDateTime.now());
        execution.setCreatedBy(triggerUser);
        taskExecutionMapper.insert(execution);

        String finalStatus = "SUCCESS";
        String errorMessage = null;
        try {
            Map<String, Object> context = new HashMap<>();
            if (inputData != null && !inputData.isEmpty()) {
                context.put("input", inputData);
                context.put("last", inputData);
            }
            for (TaskNode node : sortNodes(aggregation.getNodes(), aggregation.getEdges())) {
                executeNode(executionId, node, context);
            }
        } catch (Exception ex) {
            finalStatus = "FAILED";
            errorMessage = ex.getMessage();
        }
        taskExecutionMapper.updateStatus(executionId, finalStatus, triggerUser, errorMessage);
        updateTestStatusIfNecessary(environment, aggregation, execution.getId(), finalStatus);

        ExecutionRunVO vo = new ExecutionRunVO();
        vo.setExecutionId(executionId);
        vo.setStatus(finalStatus);
        return vo;
    }

    private TaskAggregation loadAggregation(Long taskId, String environment) {
        Result<TaskAggregation> result = taskServiceClient.getTaskAggregation(taskId, environment);
        if (result == null || result.getCode() == null || result.getCode() != 0 || result.getData() == null) {
            throw new BizException(BIZ_ERROR_CODE, "获取任务配置失败");
        }
        return result.getData();
    }

    private void updateTestStatusIfNecessary(String environment, TaskAggregation aggregation, Long executionRecordId, String finalStatus) {
        if (!"TEST".equals(environment) || aggregation.getVersionId() == null) {
            return;
        }
        TaskVersionTestStatusUpdateDTO statusDTO = new TaskVersionTestStatusUpdateDTO();
        statusDTO.setTestStatus("SUCCESS".equals(finalStatus) ? "PASSED" : "FAILED");
        statusDTO.setTestExecutionId(executionRecordId);
        taskServiceClient.updateVersionTestStatus(aggregation.getTaskId(), aggregation.getVersionId(), statusDTO);
    }

    private List<TaskNode> sortNodes(List<TaskNode> nodes, List<TaskEdge> edges) {
        if (nodes == null || nodes.isEmpty()) {
            throw new BizException(BIZ_ERROR_CODE, "DAG未配置");
        }
        Map<String, TaskNode> nodeMap = new HashMap<>();
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, List<String>> adjacency = new HashMap<>();
        for (TaskNode node : nodes) {
            if (node == null || !StringUtils.hasText(node.getNodeKey())) {
                throw new BizException(BIZ_ERROR_CODE, "DAG存在无效节点");
            }
            if (nodeMap.containsKey(node.getNodeKey())) {
                throw new BizException(BIZ_ERROR_CODE, "DAG存在重复节点：" + node.getNodeKey());
            }
            nodeMap.put(node.getNodeKey(), node);
            indegree.put(node.getNodeKey(), 0);
            adjacency.put(node.getNodeKey(), new ArrayList<>());
        }
        List<TaskEdge> sortedEdges = edges == null ? List.of() : edges.stream()
                .sorted(Comparator
                        .comparing((TaskEdge edge) -> edge.getSortOrder() == null ? 0 : edge.getSortOrder())
                        .thenComparing(edge -> edge.getSourceNodeKey() == null ? "" : edge.getSourceNodeKey())
                        .thenComparing(edge -> edge.getTargetNodeKey() == null ? "" : edge.getTargetNodeKey()))
                .toList();
        Set<String> edgeKeys = new HashSet<>();
        for (TaskEdge edge : sortedEdges) {
            if (edge == null
                    || !StringUtils.hasText(edge.getSourceNodeKey())
                    || !StringUtils.hasText(edge.getTargetNodeKey())) {
                throw new BizException(BIZ_ERROR_CODE, "DAG存在无效连线");
            }
            if (!nodeMap.containsKey(edge.getSourceNodeKey()) || !nodeMap.containsKey(edge.getTargetNodeKey())) {
                throw new BizException(BIZ_ERROR_CODE, "DAG连线端点不存在");
            }
            if (edge.getSourceNodeKey().equals(edge.getTargetNodeKey())) {
                throw new BizException(BIZ_ERROR_CODE, "DAG不允许自环：" + edge.getSourceNodeKey());
            }
            String edgeKey = edge.getSourceNodeKey() + "->" + edge.getTargetNodeKey();
            if (!edgeKeys.add(edgeKey)) {
                throw new BizException(BIZ_ERROR_CODE, "DAG存在重复连线：" + edgeKey);
            }
            adjacency.get(edge.getSourceNodeKey()).add(edge.getTargetNodeKey());
            indegree.put(edge.getTargetNodeKey(), indegree.get(edge.getTargetNodeKey()) + 1);
        }
        PriorityQueue<String> queue = new PriorityQueue<>((left, right) -> compareNode(nodeMap.get(left), nodeMap.get(right)));
        for (TaskNode node : nodes) {
            if (indegree.get(node.getNodeKey()) == 0) {
                queue.offer(node.getNodeKey());
            }
        }
        List<TaskNode> ordered = new ArrayList<>();
        while (!queue.isEmpty()) {
            String nodeKey = queue.poll();
            ordered.add(nodeMap.get(nodeKey));
            for (String target : adjacency.get(nodeKey)) {
                indegree.put(target, indegree.get(target) - 1);
                if (indegree.get(target) == 0) {
                    queue.offer(target);
                }
            }
        }
        if (ordered.size() != nodes.size()) {
            throw new BizException(BIZ_ERROR_CODE, "DAG存在环路，无法执行");
        }
        return ordered;
    }

    private int compareNode(TaskNode left, TaskNode right) {
        int positionXCompare = Integer.compare(positionValue(left.getPositionX()), positionValue(right.getPositionX()));
        if (positionXCompare != 0) {
            return positionXCompare;
        }
        int positionYCompare = Integer.compare(positionValue(left.getPositionY()), positionValue(right.getPositionY()));
        if (positionYCompare != 0) {
            return positionYCompare;
        }
        return left.getNodeKey().compareTo(right.getNodeKey());
    }

    private int positionValue(Integer value) {
        return value == null ? 0 : value;
    }

    private void executeNode(String executionId, TaskNode node, Map<String, Object> context) {
        LocalDateTime startTime = LocalDateTime.now();
        NodeExecutionLog log = new NodeExecutionLog();
        log.setExecutionId(executionId);
        log.setNodeId(node.getNodeKey());
        log.setNodeName(node.getNodeName());
        log.setNodeType(node.getNodeType());
        log.setStatus("RUNNING");
        log.setStartTime(startTime);
        log.setInputData(toJson(context));
        try {
            Object output = executeNodeByType(node, context);
            context.put(node.getNodeKey(), output);
            context.put("last", output);
            log.setStatus("SUCCESS");
            log.setOutputData(toJson(output));
        } catch (Exception ex) {
            log.setStatus("FAILED");
            log.setErrorMessage(ex.getMessage());
            log.setOutputData("{}");
            throw new BizException(BIZ_ERROR_CODE, "节点执行失败：" + node.getNodeName() + "，" + ex.getMessage());
        } finally {
            log.setEndTime(LocalDateTime.now());
            nodeExecutionLogMapper.insert(log);
        }
    }

    private Object executeNodeByType(TaskNode node, Map<String, Object> context) {
        String nodeType = StringUtils.hasText(node.getNodeType()) ? node.getNodeType().trim().toUpperCase(Locale.ROOT) : "API";
        return switch (nodeType) {
            case "START" -> context;
            case "API" -> executeApiNode(node, context);
            case "SQL" -> executeSqlNode(node, context);
            case "MAPPING" -> executeMappingNode(node, context);
            case "SCRIPT" -> throw new BizException(BIZ_ERROR_CODE, "SCRIPT节点暂未接入脚本执行器");
            case "END" -> context.getOrDefault("last", context);
            default -> throw new BizException(BIZ_ERROR_CODE, "不支持的节点类型：" + nodeType);
        };
    }

    private Object executeApiNode(TaskNode node, Map<String, Object> context) {
        Map<String, Object> config = parseObject(node.getConfigJson());
        String url = resolveApiUrl(node, config);
        if (!StringUtils.hasText(url)) {
            throw new BizException(BIZ_ERROR_CODE, "API节点缺少url配置");
        }
        String method = StringUtils.hasText(stringValue(config.get("method"))) ? stringValue(config.get("method")).toUpperCase(Locale.ROOT) : "GET";
        Object body = config.containsKey("body") ? config.get("body") : context.get("last");
        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.valueOf(method), new HttpEntity<>(body, resolveHeaders(config)), Object.class);
        return response.getBody();
    }

    private Object executeSqlNode(TaskNode node, Map<String, Object> context) {
        Map<String, Object> config = parseObject(node.getConfigJson());
        Datasource datasource = resolveDatasource(node, DATASOURCE_TYPE_MYSQL, "SQL节点只能引用MYSQL数据源");
        String sql = stringValue(config.get("sql"));
        if (!StringUtils.hasText(sql)) {
            throw new BizException(BIZ_ERROR_CODE, "SQL节点缺少sql配置");
        }
        String trimmedSql = sql.trim();
        validateReadonlySql(trimmedSql);
        ParsedSql parsedSql = parseNamedSql(trimmedSql);
        Map<String, Object> params = resolveSqlParams(config.get("params"), context);
        String resultType = StringUtils.hasText(stringValue(config.get("resultType")))
                ? stringValue(config.get("resultType")).toUpperCase(Locale.ROOT)
                : "LIST";
        try (Connection connection = DriverManager.getConnection(datasource.getJdbcUrl(), datasource.getUsername(), datasource.getPassword());
             PreparedStatement statement = connection.prepareStatement(parsedSql.sql())) {
            for (int index = 0; index < parsedSql.parameterNames().size(); index++) {
                String paramName = parsedSql.parameterNames().get(index);
                if (!params.containsKey(paramName)) {
                    throw new BizException(BIZ_ERROR_CODE, "SQL参数未配置：" + paramName);
                }
                statement.setObject(index + 1, params.get(paramName));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> rows = readRows(resultSet);
                if ("ONE".equals(resultType)) {
                    return rows.isEmpty() ? null : rows.get(0);
                }
                return rows;
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(BIZ_ERROR_CODE, "SQL执行失败：" + ex.getMessage());
        }
    }

    private void validateReadonlySql(String sql) {
        String lowerSql = sql.toLowerCase(Locale.ROOT);
        if (!lowerSql.startsWith("select")) {
            throw new BizException(BIZ_ERROR_CODE, "SQL节点第一阶段仅允许SELECT查询");
        }
        String sqlWithoutTrailingSemicolon = lowerSql.endsWith(";") ? lowerSql.substring(0, lowerSql.length() - 1) : lowerSql;
        if (sqlWithoutTrailingSemicolon.contains(";")) {
            throw new BizException(BIZ_ERROR_CODE, "SQL节点不允许执行多语句");
        }
        if (WRITE_SQL_PATTERN.matcher(sql).find()) {
            throw new BizException(BIZ_ERROR_CODE, "SQL节点不允许执行写操作或DDL语句");
        }
    }

    private String resolveApiUrl(TaskNode node, Map<String, Object> config) {
        String directUrl = stringValue(config.get("url"));
        if (StringUtils.hasText(directUrl)) {
            return directUrl;
        }
        Datasource datasource = resolveDatasource(node, DATASOURCE_TYPE_HTTP_API, "API节点只能引用HTTP_API数据源");
        String baseUrl = trimRight(datasource.getJdbcUrl(), "/");
        String path = stringValue(config.get("path"));
        if (!StringUtils.hasText(path)) {
            return baseUrl;
        }
        return baseUrl + "/" + trimLeft(path, "/");
    }

    private Datasource resolveDatasource(TaskNode node, String datasourceType, String typeErrorMessage) {
        if (node.getRefResourceId() == null) {
            throw new BizException(BIZ_ERROR_CODE, "节点缺少数据源配置");
        }
        if (StringUtils.hasText(node.getRefResourceType())
                && !REF_RESOURCE_TYPE_DATASOURCE.equalsIgnoreCase(node.getRefResourceType())) {
            throw new BizException(BIZ_ERROR_CODE, "节点仅支持引用DATASOURCE资源");
        }
        Datasource datasource = loadDatasource(node.getRefResourceId());
        if (!datasourceType.equalsIgnoreCase(datasource.getDatasourceType())) {
            throw new BizException(BIZ_ERROR_CODE, typeErrorMessage);
        }
        if (!"PUBLISHED".equals(datasource.getStatus())) {
            throw new BizException(BIZ_ERROR_CODE, "数据源未发布");
        }
        if (!StringUtils.hasText(datasource.getJdbcUrl())) {
            throw new BizException(BIZ_ERROR_CODE, "数据源连接信息不能为空");
        }
        return datasource;
    }

    private HttpHeaders resolveHeaders(Map<String, Object> config) {
        HttpHeaders headers = new HttpHeaders();
        Object headersValue = config.get("headers");
        if (headersValue instanceof Map<?, ?> headersMap) {
            for (Map.Entry<?, ?> entry : headersMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    headers.add(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
            }
        }
        return headers;
    }

    private Datasource loadDatasource(Long datasourceId) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= DATASOURCE_LOAD_MAX_ATTEMPTS; attempt++) {
            try {
                Result<Datasource> result = datasourceServiceClient.get(datasourceId);
                if (result != null && result.getCode() != null && result.getCode() == 0 && result.getData() != null) {
                    return result.getData();
                }
                String message = result == null ? null : result.getMessage();
                throw new BizException(BIZ_ERROR_CODE, StringUtils.hasText(message) ? message : "数据源服务返回空配置");
            } catch (Exception ex) {
                lastException = ex;
                if (attempt < DATASOURCE_LOAD_MAX_ATTEMPTS) {
                    sleepBeforeRetry(attempt);
                }
            }
        }
        String message = lastException == null ? "未知错误" : lastException.getMessage();
        throw new BizException(BIZ_ERROR_CODE, "获取数据源配置失败，datasourceId=" + datasourceId + "，" + message);
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(200L * attempt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BizException(BIZ_ERROR_CODE, "获取数据源配置被中断");
        }
    }

    private String trimLeft(String value, String token) {
        String result = value;
        while (result.startsWith(token)) {
            result = result.substring(token.length());
        }
        return result;
    }

    private String trimRight(String value, String token) {
        String result = value;
        while (result.endsWith(token)) {
            result = result.substring(0, result.length() - token.length());
        }
        return result;
    }

    private ParsedSql parseNamedSql(String sql) {
        StringBuilder parsedSql = new StringBuilder();
        List<String> parameterNames = new ArrayList<>();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int index = 0; index < sql.length(); index++) {
            char current = sql.charAt(index);
            if (current == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                parsedSql.append(current);
                continue;
            }
            if (current == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                parsedSql.append(current);
                continue;
            }
            if (current == ':' && !inSingleQuote && !inDoubleQuote && index + 1 < sql.length()
                    && Character.isJavaIdentifierStart(sql.charAt(index + 1))) {
                int end = index + 2;
                while (end < sql.length() && Character.isJavaIdentifierPart(sql.charAt(end))) {
                    end++;
                }
                parameterNames.add(sql.substring(index + 1, end));
                parsedSql.append('?');
                index = end - 1;
                continue;
            }
            parsedSql.append(current);
        }
        return new ParsedSql(parsedSql.toString(), parameterNames);
    }

    private Map<String, Object> resolveSqlParams(Object paramsValue, Map<String, Object> context) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (!(paramsValue instanceof Map<?, ?> paramsMap)) {
            return params;
        }
        for (Map.Entry<?, ?> entry : paramsMap.entrySet()) {
            if (entry.getKey() != null) {
                params.put(String.valueOf(entry.getKey()), resolveExpression(entry.getValue(), context));
            }
        }
        return params;
    }

    private Object resolveExpression(Object value, Map<String, Object> context) {
        if (!(value instanceof String expression) || !expression.startsWith("$.")) {
            return value;
        }
        String[] parts = expression.substring(2).split("\\.");
        Object current = context;
        for (String part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    private List<Map<String, Object>> readRows(ResultSet resultSet) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int index = 1; index <= columnCount; index++) {
                String label = metaData.getColumnLabel(index);
                row.put(StringUtils.hasText(label) ? label : metaData.getColumnName(index), resultSet.getObject(index));
            }
            rows.add(row);
        }
        return rows;
    }

    private record ParsedSql(String sql, List<String> parameterNames) {
    }

    private Object executeMappingNode(TaskNode node, Map<String, Object> context) {
        Map<String, Object> config = parseObject(node.getConfigJson());
        Object source = config.containsKey("source") ? config.get("source") : context.get("last");
        Map<String, Object> sourceMap = source instanceof Map<?, ?> map ? normalizeMap(map) : parseObject(toJson(source));
        Object mappingsValue = config.get("mappings");
        if (!(mappingsValue instanceof Map<?, ?> mappings)) {
            return sourceMap;
        }
        Map<String, Object> output = new HashMap<>();
        for (Map.Entry<?, ?> entry : mappings.entrySet()) {
            String targetKey = String.valueOf(entry.getKey());
            String sourceKey = String.valueOf(entry.getValue());
            output.put(targetKey, sourceMap.get(sourceKey));
        }
        return output;
    }

    private Map<String, Object> parseObject(String json) {
        if (!StringUtils.hasText(json)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new BizException(BIZ_ERROR_CODE, "节点配置JSON格式错误");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private Map<String, Object> normalizeMap(Map<?, ?> source) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
