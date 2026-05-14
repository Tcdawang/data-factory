CREATE DATABASE IF NOT EXISTS data_factory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE data_factory;

CREATE TABLE IF NOT EXISTS df_task_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务分类ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID，逻辑外键',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    UNIQUE KEY uk_parent_name_deleted (parent_id, category_name, deleted),
    KEY idx_parent_id (parent_id),
    KEY idx_sort_no (sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分类表';

CREATE TABLE IF NOT EXISTS df_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_code VARCHAR(100) NOT NULL COMMENT '任务编码',
    category_id BIGINT DEFAULT NULL COMMENT '任务分类ID，逻辑外键',
    description VARCHAR(500) DEFAULT NULL COMMENT '任务描述',
    status VARCHAR(30) NOT NULL DEFAULT 'UNPUBLISHED' COMMENT '任务状态：UNPUBLISHED/PUBLISHED/DISABLED',
    current_version_id BIGINT DEFAULT NULL COMMENT '当前版本ID，逻辑外键',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    UNIQUE KEY uk_task_name_deleted (task_name, deleted),
    UNIQUE KEY uk_task_code_deleted (task_code, deleted),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_current_version_id (current_version_id),
    KEY idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务主表';

CREATE TABLE IF NOT EXISTS df_task_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务版本ID',
    task_id BIGINT NOT NULL COMMENT '任务ID，逻辑外键',
    version_no VARCHAR(50) NOT NULL COMMENT '版本号',
    env VARCHAR(20) NOT NULL DEFAULT 'DEV' COMMENT '环境：DEV/TEST/PROD',
    version_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '版本状态：DRAFT/TEST_PASSED/PUBLISHED/ARCHIVED',
    publish_status VARCHAR(30) NOT NULL DEFAULT 'UNPUBLISHED' COMMENT '发布状态：UNPUBLISHED/PUBLISHED/DISABLED',
    dag_json LONGTEXT NOT NULL COMMENT 'DAG画布JSON',
    dsl_json LONGTEXT NOT NULL COMMENT '任务DSL JSON',
    input_schema_json LONGTEXT DEFAULT NULL COMMENT '任务入参Schema',
    output_schema_json LONGTEXT DEFAULT NULL COMMENT '任务出参Schema',
    test_status VARCHAR(30) NOT NULL DEFAULT 'UNTESTED' COMMENT '测试状态：UNTESTED/PASSED/FAILED',
    test_execution_id BIGINT DEFAULT NULL COMMENT '最近一次测试执行ID，逻辑外键',
    rollback_from_version_id BIGINT DEFAULT NULL COMMENT '回滚来源版本ID，逻辑外键',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    UNIQUE KEY uk_task_env_version_deleted (task_id, env, version_no, deleted),
    KEY idx_task_id (task_id),
    KEY idx_env (env),
    KEY idx_version_status (version_status),
    KEY idx_publish_status (publish_status),
    KEY idx_test_status (test_status),
    KEY idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务版本表';

CREATE TABLE IF NOT EXISTS df_task_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '节点ID',
    task_id BIGINT NOT NULL COMMENT '任务ID，逻辑外键',
    task_version_id BIGINT NOT NULL COMMENT '任务版本ID，逻辑外键',
    node_key VARCHAR(100) NOT NULL COMMENT '节点唯一Key',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(50) NOT NULL COMMENT '节点类型：START/API/SCRIPT/MAPPING/OUTPUT/END',
    component_id BIGINT DEFAULT NULL COMMENT '组件ID，逻辑外键',
    ref_resource_id BIGINT DEFAULT NULL COMMENT '引用资源ID，如API ID、脚本ID、数据源ID',
    ref_resource_type VARCHAR(50) DEFAULT NULL COMMENT '引用资源类型：API_SOURCE/SCRIPT/DATASOURCE',
    config_json LONGTEXT DEFAULT NULL COMMENT '节点配置JSON',
    input_mapping_json LONGTEXT DEFAULT NULL COMMENT '输入映射JSON',
    output_schema_json LONGTEXT DEFAULT NULL COMMENT '输出Schema JSON',
    position_x INT DEFAULT 0 COMMENT '画布X坐标',
    position_y INT DEFAULT 0 COMMENT '画布Y坐标',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    UNIQUE KEY uk_version_node_key_deleted (task_version_id, node_key, deleted),
    KEY idx_task_id (task_id),
    KEY idx_task_version_id (task_version_id),
    KEY idx_node_type (node_type),
    KEY idx_ref_resource (ref_resource_type, ref_resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务节点表';

CREATE TABLE IF NOT EXISTS df_task_edge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '连线ID',
    task_id BIGINT NOT NULL COMMENT '任务ID，逻辑外键',
    task_version_id BIGINT NOT NULL COMMENT '任务版本ID，逻辑外键',
    source_node_key VARCHAR(100) NOT NULL COMMENT '上游节点Key',
    target_node_key VARCHAR(100) NOT NULL COMMENT '下游节点Key',
    condition_expr VARCHAR(1000) DEFAULT NULL COMMENT '条件表达式',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    UNIQUE KEY uk_version_edge_deleted (task_version_id, source_node_key, target_node_key, deleted),
    KEY idx_task_id (task_id),
    KEY idx_task_version_id (task_version_id),
    KEY idx_source_node_key (source_node_key),
    KEY idx_target_node_key (target_node_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务连线表';

CREATE TABLE IF NOT EXISTS df_task_publish_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '发布记录ID',
    task_id BIGINT NOT NULL COMMENT '任务ID，逻辑外键',
    task_version_id BIGINT NOT NULL COMMENT '任务版本ID，逻辑外键',
    source_env VARCHAR(20) DEFAULT NULL COMMENT '来源环境',
    target_env VARCHAR(20) NOT NULL COMMENT '目标环境',
    publish_type VARCHAR(30) NOT NULL COMMENT '发布类型：PUBLISH/DISABLE/ROLLBACK',
    before_version_id BIGINT DEFAULT NULL COMMENT '变更前版本ID，逻辑外键',
    after_version_id BIGINT DEFAULT NULL COMMENT '变更后版本ID，逻辑外键',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    KEY idx_task_id (task_id),
    KEY idx_task_version_id (task_version_id),
    KEY idx_target_env (target_env),
    KEY idx_publish_type (publish_type),
    KEY idx_operated_at (operated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务发布记录表';

CREATE TABLE IF NOT EXISTS df_task_trigger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '触发配置ID',
    task_id BIGINT NOT NULL COMMENT '任务ID，逻辑外键',
    task_version_id BIGINT NOT NULL COMMENT '任务版本ID，逻辑外键',
    trigger_type VARCHAR(30) NOT NULL COMMENT '触发类型：MANUAL/API/SCHEDULE',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否，1是',
    open_api_id BIGINT DEFAULT NULL COMMENT '开放接口ID，逻辑外键',
    schedule_id BIGINT DEFAULT NULL COMMENT '定时任务ID，逻辑外键',
    default_input_json LONGTEXT DEFAULT NULL COMMENT '默认入参JSON',
    config_json LONGTEXT DEFAULT NULL COMMENT '触发配置JSON',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
    KEY idx_task_id (task_id),
    KEY idx_task_version_id (task_version_id),
    KEY idx_trigger_type (trigger_type),
    KEY idx_open_api_id (open_api_id),
    KEY idx_schedule_id (schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务触发配置表';

CREATE TABLE IF NOT EXISTS df_task_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务执行ID',
    trace_id VARCHAR(100) NOT NULL COMMENT '链路追踪ID',
    task_id BIGINT NOT NULL COMMENT '任务ID，逻辑外键',
    task_version_id BIGINT NOT NULL COMMENT '任务版本ID，逻辑外键',
    trigger_type VARCHAR(30) NOT NULL COMMENT '触发类型：MANUAL/API/SCHEDULE/TEST',
    trigger_source VARCHAR(100) DEFAULT NULL COMMENT '触发来源',
    execution_status VARCHAR(30) NOT NULL COMMENT '执行状态：RUNNING/SUCCESS/FAILED/TIMEOUT/CANCELED',
    input_json LONGTEXT DEFAULT NULL COMMENT '任务入参',
    output_json LONGTEXT DEFAULT NULL COMMENT '任务出参',
    error_message TEXT DEFAULT NULL COMMENT '错误信息',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    duration_ms BIGINT DEFAULT NULL COMMENT '耗时毫秒',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_trace_id (trace_id),
    KEY idx_task_id (task_id),
    KEY idx_task_version_id (task_version_id),
    KEY idx_execution_status (execution_status),
    KEY idx_trigger_type (trigger_type),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

CREATE TABLE IF NOT EXISTS df_node_execution_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    execution_id VARCHAR(64) NOT NULL COMMENT '执行批次ID',
    node_id VARCHAR(64) NOT NULL COMMENT '节点ID',
    node_name VARCHAR(128) DEFAULT NULL COMMENT '节点名称',
    node_type VARCHAR(32) NOT NULL COMMENT '节点类型',
    status VARCHAR(20) NOT NULL COMMENT '节点执行状态: RUNNING/SUCCESS/FAILED/SKIPPED',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    input_data TEXT DEFAULT NULL COMMENT '输入数据JSON',
    output_data TEXT DEFAULT NULL COMMENT '输出数据JSON',
    error_message TEXT DEFAULT NULL COMMENT '错误信息',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_execution_id (execution_id),
    KEY idx_node_id (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点执行日志表';

CREATE TABLE IF NOT EXISTS df_task_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    task_code VARCHAR(64) NOT NULL COMMENT '任务编码',
    environment VARCHAR(20) NOT NULL COMMENT '环境: DEV/TEST/PROD',
    execution_id VARCHAR(64) NOT NULL COMMENT '执行批次ID',
    status VARCHAR(20) NOT NULL DEFAULT 'INIT' COMMENT '状态: INIT/RUNNING/SUCCESS/FAILED/STOPPED',
    trigger_type VARCHAR(20) NOT NULL COMMENT '触发方式: MANUAL/SCHEDULED/API',
    trigger_user VARCHAR(64) DEFAULT NULL COMMENT '触发人',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    error_message TEXT DEFAULT NULL COMMENT '错误信息',
    delete_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0否 1是',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_execution_id (execution_id),
    KEY idx_task_id (task_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行记录表';

CREATE TABLE IF NOT EXISTS df_node_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '节点执行ID',
    task_execution_id BIGINT NOT NULL COMMENT '任务执行ID，逻辑外键',
    task_id BIGINT NOT NULL COMMENT '任务ID，逻辑外键',
    task_version_id BIGINT NOT NULL COMMENT '任务版本ID，逻辑外键',
    node_key VARCHAR(100) NOT NULL COMMENT '节点Key',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(50) NOT NULL COMMENT '节点类型',
    execution_status VARCHAR(30) NOT NULL COMMENT '执行状态：RUNNING/SUCCESS/FAILED/SKIPPED',
    input_json LONGTEXT DEFAULT NULL COMMENT '节点入参',
    output_json LONGTEXT DEFAULT NULL COMMENT '节点出参',
    error_message TEXT DEFAULT NULL COMMENT '错误信息',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    duration_ms BIGINT DEFAULT NULL COMMENT '耗时毫秒',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_task_execution_id (task_execution_id),
    KEY idx_task_id (task_id),
    KEY idx_task_version_id (task_version_id),
    KEY idx_node_key (node_key),
    KEY idx_execution_status (execution_status),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点执行日志表';
