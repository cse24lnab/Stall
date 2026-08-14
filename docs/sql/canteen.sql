CREATE DATABASE IF NOT EXISTS canteen;
USE canteen;

-- 1. 创建摊位表 (Stall)
CREATE TABLE `stall` (
    `id`                 INT NOT NULL AUTO_INCREMENT COMMENT '摊位ID',
    `name`               VARCHAR(100) NOT NULL COMMENT '摊位名称',
    `current_status`     INT NOT NULL DEFAULT 0 COMMENT '状态: 0=休息, 1=营业',

    -- 地点信息
    `noon_location`      VARCHAR(100) DEFAULT NULL COMMENT '中午出摊点',
    `evening_location`   VARCHAR(100) DEFAULT NULL COMMENT '晚上出摊点',

    -- 时间信息
    `noon_start_time`    TIME DEFAULT NULL COMMENT '中午开始时间',
    `noon_end_time`      TIME DEFAULT NULL COMMENT '中午结束时间',
    `evening_start_time` TIME DEFAULT NULL COMMENT '晚上开始时间',
    `evening_end_time`   TIME DEFAULT NULL COMMENT '晚上结束时间',
    `owner_user_id`      INT NOT NULL COMMENT '所属商家用户ID',

    -- 自动记录时间
    `create_time`        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `is_delete`          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-正常, 1-已删除',
    `active_name`        VARCHAR(100) GENERATED ALWAYS AS (
        CASE WHEN `is_delete` = 0 THEN `name` ELSE NULL END
    ) STORED COMMENT '仅有效记录保留名称，用于逻辑删除唯一约束',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_stall_active_name` (`active_name`),
    KEY `idx_stall_owner_delete_id` (`owner_user_id`, `is_delete`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摊位基本信息表';

-- 2. 创建菜品表 (Dish)
CREATE TABLE `dish` (
    `id`          INT NOT NULL AUTO_INCREMENT COMMENT '菜品ID',
    `stall_id`    INT NOT NULL COMMENT '所属摊位ID，由应用层校验关联关系',
    `name`        VARCHAR(100) NOT NULL COMMENT '菜品名称',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `is_sold_out` INT NOT NULL DEFAULT 0 COMMENT '状态: 0=有货, 1=售罄',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_delete`   TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-正常, 1-已删除',
    `active_name` VARCHAR(100) GENERATED ALWAYS AS (
        CASE WHEN `is_delete` = 0 THEN `name` ELSE NULL END
    ) STORED COMMENT '仅有效记录保留名称，用于逻辑删除唯一约束',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dish_stall_active_name` (`stall_id`, `active_name`),
    KEY `idx_dish_stall_delete_id` (`stall_id`, `is_delete`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摊位菜品表';

-- 3. 创建用户表 (User)
CREATE TABLE `user` (
    `id`             INT NOT NULL AUTO_INCREMENT COMMENT '用户id',
    `username`       VARCHAR(25) NOT NULL COMMENT '用户名',
    `password_hash`  VARCHAR(255) NOT NULL COMMENT '密码哈希值（如 BCrypt）',
    `nickname`       VARCHAR(25) NOT NULL COMMENT '用户昵称',
    `phone`          VARCHAR(11) DEFAULT NULL COMMENT '用户手机号',
    `avatar_file_id` INT DEFAULT NULL COMMENT '头像文件 ID',
    `avatar_url`     VARCHAR(255) DEFAULT NULL COMMENT '用户头像访问地址',
    `role`           TINYINT NOT NULL DEFAULT 0 COMMENT '用户角色，0=USER，1=MERCHANT，2=ADMIN',
    `status`         TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态，1=ACTIVE，0=DISABLED',
    `create_time`    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除逻辑判断位,0为不删除',

    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`),
    UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 4. 给 stall 初始化一个数据
INSERT INTO `stall`
(`id`, `name`, `current_status`, `noon_location`, `evening_location`, `noon_start_time`, `noon_end_time`, `evening_start_time`, `evening_end_time`, `owner_user_id`, `create_time`, `update_time`)
VALUES
    (1, '烤冷面', 1, '东一门', '西二门', '12:00:00', '13:00:00', '17:00:00', '23:00:00', 2, NOW(), NOW());

-- 5. 给 dish 初始化一个数据
INSERT INTO `dish`
(`id`, `stall_id`, `name`, `price`, `is_sold_out`, `create_time`, `update_time`)
VALUES
    (1, 1, '基础款烤冷面', 8.00, 0, NOW(), NOW());

-- 6. 给 user 初始化演示账号
INSERT INTO `user`
(`username`, `password_hash`, `nickname`, `role`)
VALUES
    ('user_demo', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '普通用户演示账号', 0),
    ('merchant_demo', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '商家演示账号', 1),
    ('admin_demo', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '管理员演示账号', 2);
