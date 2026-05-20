CREATE DATABASE canteen;
USE canteen;
-- 1. 创建摊位表 (Stall)
CREATE TABLE `stall` (
    `id`                 INT NOT NULL AUTO_INCREMENT COMMENT '摊位ID',
    `name`               VARCHAR(100) NOT NULL COMMENT '摊位名称',
    `current_status`     INT NOT NULL DEFAULT 0 COMMENT '状态: 0=休息, 1=营业',
    
    -- 地点信息
    `noon_location`      VARCHAR(100) COMMENT '中午出摊点',
    `evening_location`   VARCHAR(100) COMMENT '晚上出摊点',
    
    -- 时间信息
    `noon_start_time`    TIME COMMENT '中午开始时间',
    `noon_end_time`      TIME COMMENT '中午结束时间',
    `evening_start_time` TIME COMMENT '晚上开始时间',
    `evening_end_time`   TIME COMMENT '晚上结束时间',
    
    -- 自动记录时间的神器
    `create_time`        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摊位基本信息表';

-- 2. 创建菜品表 (Dish)
CREATE TABLE `dish` (
    `id`          INT NOT NULL AUTO_INCREMENT COMMENT '菜品ID',
    `stall_id`    INT NOT NULL COMMENT '所属摊位ID (外键)',
    `name`        VARCHAR(100) NOT NULL COMMENT '菜品名称',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '价格',
    `is_sold_out` INT NOT NULL DEFAULT 0 COMMENT '状态: 0=有货, 1=售罄',
    
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (`id`),
    -- 外键约束
    CONSTRAINT `fk_dish_stall` FOREIGN KEY (`stall_id`) REFERENCES `stall` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摊位菜品表';

-- 3.给stall初始化一个数据
INSERT INTO `stall`
(`id`,`name`,`current_status`,`noon_location`,`evening_location`,`noon_start_time`,`noon_end_time`,`evening_start_time`,`evening_end_time`,`create_time`,`update_time`) 
VALUES (
    '1',
    '烤冷面',
    '1',
    '东一门',
    '西二门',
    '12:00:00',
    '13:00:00',
    '17:00:00',
    '23:00:00',
    NOW(),
    NOW());

-- 4.给dish初始化一个数据
INSERT INTO `dish`
(`id`,`stall_id`,`name`,`price`,`is_sold_out`,`create_time`,`update_time`)
VALUES (
    '1',
    '1',
    '基础款烤冷面',
    '8',
    '0',
    NOW(),
    NOW()
);

CREATE TABLE `user`(
    id INT AUTO_INCREMENT NOT NULL COMMENT "用户id",
    username VARCHAR(25) NOT NULL UNIQUE COMMENT "用户名",
    nickname VARCHAR(25) NOT NULL COMMENT "用户昵称",
    phone varchar(11) comment "用户手机号",
    avata_url varchar(100) comment "用户头像路径",
    role INT NOT NULL default 0 COMMENT "用户身份，0是顾客，1是商家，2是管理者",
    create_time DATETIME default CURRENT_TIMESTAMP comment "创建时间",
    update_time datetime default current_timestamp on update current_timestamp comment "更新时间",
    is_delete BOOLEAN NOT NULL  default 0 comment "是否删除逻辑判断位,0为不删除",
    primary key (id)
) engine = InnoDB default charset =utf8mb4 comment ="用户信息表";

desc `user`;

ALTER TABLE `user`
    CHANGE COLUMN `avata_url` `avatar_url` VARCHAR(255) NULL COMMENT '用户头像访问地址',
    MODIFY COLUMN `role` INT NOT NULL DEFAULT 0 COMMENT '用户角色，0=USER，1=MERCHANT，2=ADMIN',
    ADD COLUMN `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希值（如 BCrypt）' AFTER `username`,
    ADD COLUMN `avatar_file_id` INT NULL COMMENT '头像文件 ID' AFTER `phone`,
    ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态，1=ACTIVE，0=DISABLED' AFTER `role`,
    ADD UNIQUE KEY `uk_user_phone` (`phone`);


ALTER TABLE `user` MODIFY COLUMN `role` tinyint NOT NULL DEFAULT 0 COMMENT '用户角色，0=USER，1=MERCHANT，2=ADMIN';

INSERT INTO `user`
(`username`, `password_hash`, `nickname`, `role`)
VALUES
    ('user_demo', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '普通用户演示账号',  0 ),
    ('merchant_demo', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '商家演示账号' ,1),
    ('admin_demo', '$2b$10$s6flCBjbZplCftbcZu3GVOfaFI.PipuuaRQkyQOwOZHG2DTkg1Hgy', '管理员演示账号',  2 );
