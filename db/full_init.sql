-- IT运维综合管理系统 数据库初始化脚本
USE itoms;

-- 1. 部门表
CREATE TABLE IF NOT EXISTS `department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级部门ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 2. 资产表
CREATE TABLE IF NOT EXISTS `asset` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `asset_code` VARCHAR(50) NOT NULL COMMENT '资产编号(系统自动生成)',
    `asset_name` VARCHAR(100) NOT NULL COMMENT '资产名称',
    `category_id` BIGINT NOT NULL COMMENT '资产分类ID',
    `serial_number` VARCHAR(100) DEFAULT NULL COMMENT '序列号',
    `purchase_date` DATE NOT NULL COMMENT '采购日期',
    `guarantee_months` INT DEFAULT 36 COMMENT '保质期(月)',
    `department_id` BIGINT NOT NULL COMMENT '归属部门ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '使用人ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-在用, 2-闲置, 3-维修, 4-报废)',
    `health_status` VARCHAR(20) DEFAULT 'GOOD' COMMENT '健康度(GOOD, NORMAL, POOR)',
    `location` VARCHAR(255) DEFAULT NULL COMMENT '存放位置',
    `qr_code_url` VARCHAR(500) DEFAULT NULL COMMENT '二维码图片链接',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_code` (`asset_code`),
    INDEX `idx_status_location` (`status`, `location`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IT资产表';

-- 3. 工单表
CREATE TABLE IF NOT EXISTS `work_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `work_order_code` VARCHAR(50) NOT NULL COMMENT '工单编号',
    `fault_type` VARCHAR(50) NOT NULL COMMENT '故障类型',
    `description` TEXT NOT NULL COMMENT '问题描述',
    `urgency_level` TINYINT NOT NULL DEFAULT 1 COMMENT '紧急程度(1-普通, 2-紧急, 3-特急)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-待处理, 2-处理中, 3-待验收, 4-已完成, 5-已关闭/驳回)',
    `creator_id` BIGINT NOT NULL COMMENT '报修人ID',
    `department_id` BIGINT NOT NULL COMMENT '报修人部门ID',
    `assignee_id` BIGINT DEFAULT NULL COMMENT '维修人ID',
    `asset_id` BIGINT DEFAULT NULL COMMENT '关联资产ID',
    `priority_level` INT DEFAULT 1 COMMENT 'AI计算优先级',
    `sla_deadline` DATETIME DEFAULT NULL COMMENT 'SLA截止时间',
    `evaluate_score` TINYINT DEFAULT NULL COMMENT '评价星级(1-5)',
    `evaluate_remark` VARCHAR(500) DEFAULT NULL COMMENT '评价备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_work_order_code` (`work_order_code`),
    INDEX `idx_status_creator` (`status`, `creator_id`, `create_time`),
    INDEX `idx_assignee_status` (`assignee_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障报修工单表';

-- 4. 工单流转日志表
CREATE TABLE IF NOT EXISTS `work_order_flow_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `work_order_id` BIGINT NOT NULL COMMENT '工单ID',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `action` VARCHAR(50) NOT NULL COMMENT '操作动作(CREATE, ASSIGN, PROCESS, FINISH, EVALUATE)',
    `previous_status` TINYINT DEFAULT NULL COMMENT '变更前状态',
    `current_status` TINYINT NOT NULL COMMENT '变更后状态',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '操作备注(如维修详情)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_work_order_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单流转日志表';

-- 5. 问题库/故障知识库表
CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `fault_type` VARCHAR(50) NOT NULL COMMENT '故障类型',
    `symptom` TEXT NOT NULL COMMENT '故障现象',
    `solution` TEXT NOT NULL COMMENT '解决方案',
    `ai_generated_flag` TINYINT DEFAULT 0 COMMENT '是否AI生成(0-人工, 1-AI)',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0-禁用, 1-启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    FULLTEXT KEY `ft_symptom_solution` (`symptom`, `solution`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 6. AI推荐日志表
CREATE TABLE IF NOT EXISTS `ai_recommendation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `work_order_id` BIGINT DEFAULT NULL COMMENT '工单ID(如有)',
    `description_input` TEXT COMMENT '输入描述',
    `recommended_solution_id` BIGINT COMMENT '推荐知识库ID',
    `confidence_score` DECIMAL(5,4) COMMENT '置信度',
    `user_feedback` TINYINT DEFAULT 0 COMMENT '用户反馈(1-采纳, 2-未采纳)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐日志表';

-- 7. AI接口配置表
CREATE TABLE IF NOT EXISTS `ai_api_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `provider_name` VARCHAR(50) NOT NULL COMMENT 'AI服务提供商(如 Baidu, DeepSeek 等)',
    `api_key` VARCHAR(255) NOT NULL COMMENT 'API Key',
    `secret_key` VARCHAR(255) DEFAULT NULL COMMENT 'Secret Key',
    `api_url` VARCHAR(255) DEFAULT NULL COMMENT '接口地址',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
    `is_active` TINYINT DEFAULT 0 COMMENT '是否激活(0-禁用, 1-启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_provider` (`provider_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI接口配置表';

-- 8. 消息表
CREATE TABLE IF NOT EXISTS `sys_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `receiver_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `content` TEXT NOT NULL,
    `type` VARCHAR(20) NOT NULL COMMENT 'SYSTEM, WORK_ORDER, WARNING',
    `is_read` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息表';

-- 9. 字典表
CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `dict_code` VARCHAR(50) NOT NULL,
    `dict_name` VARCHAR(50) NOT NULL,
    `dict_value` VARCHAR(100) NOT NULL,
    `sort_order` INT DEFAULT 0,
    `is_active` TINYINT DEFAULT 1,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

-- 10. 参数配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(50) NOT NULL,
    `config_value` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';

-- 11. 菜单表
CREATE TABLE IF NOT EXISTS `menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `path` VARCHAR(100) NOT NULL,
    `component` VARCHAR(255),
    `icon` VARCHAR(50),
    `parent_id` BIGINT DEFAULT 0,
    `sort_order` INT DEFAULT 0,
    `is_active` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 12. 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_name` VARCHAR(50) NOT NULL,
    `role_code` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 13. 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `operator_id` BIGINT NOT NULL,
    `operator_name` VARCHAR(50) NOT NULL,
    `module` VARCHAR(50) NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `request_method` VARCHAR(20),
    `request_url` VARCHAR(255),
    `request_ip` VARCHAR(50),
    `request_params` TEXT,
    `response_data` TEXT,
    `error_msg` TEXT,
    `status` TINYINT DEFAULT 0,
    `execute_time` BIGINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_operator_module` (`operator_id`, `module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
