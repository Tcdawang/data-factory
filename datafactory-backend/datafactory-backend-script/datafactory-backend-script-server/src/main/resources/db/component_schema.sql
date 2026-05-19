

-- 组件信息表
CREATE TABLE component_info (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL COMMENT '组件名称',
    type            VARCHAR(50) NOT NULL COMMENT '组件类型：database/api/execution',
    sub_type        VARCHAR(50) COMMENT '子类型：mysql_read/mysql_write/mongo_query/http_get/http_post',
    description     TEXT COMMENT '组件描述',
    icon            VARCHAR(100) COMMENT '组件图标',
    version         VARCHAR(20) DEFAULT '1.0.0' COMMENT '组件版本',
    status          VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/deprecated',
    created_by      BIGINT COMMENT '创建人ID',
    updated_by      BIGINT COMMENT '更新人ID',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted         INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
);

-- 组件参数表
CREATE TABLE component_param (
    id              BIGSERIAL PRIMARY KEY,
    component_id    BIGINT NOT NULL COMMENT '关联组件ID',
    param_name      VARCHAR(100) NOT NULL COMMENT '参数显示名称',
    param_key       VARCHAR(100) NOT NULL COMMENT '参数键',
    param_type      VARCHAR(50) NOT NULL COMMENT '类型：string/number/boolean/select/password',
    required        BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    default_value   VARCHAR(500) COMMENT '默认值',
    options         JSON COMMENT '下拉选项 [{"label":"是","value":"true"}]',
    placeholder     VARCHAR(200) COMMENT '占位提示',
    sort_order      INT DEFAULT 0 COMMENT '排序序号',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (component_id) REFERENCES component_info(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_component_info_type ON component_info(type);
CREATE INDEX idx_component_info_status ON component_info(status);
CREATE INDEX idx_component_info_deleted ON component_info(deleted);
CREATE INDEX idx_component_param_component_id ON component_param(component_id);
