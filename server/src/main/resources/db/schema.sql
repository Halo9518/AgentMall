-- AgentMall 数据库初始化脚本
-- 仅在数据库首次创建时自动执行（docker-entrypoint-initdb.d）

CREATE DATABASE IF NOT EXISTS agentmall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agentmall;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `phone`      VARCHAR(11)  NOT NULL COMMENT '手机号',
    `password`   VARCHAR(128) NOT NULL COMMENT 'BCrypt 加密密码',
    `nickname`   VARCHAR(32)  DEFAULT '' COMMENT '昵称',
    `avatar`     VARCHAR(256) DEFAULT '' COMMENT '头像 URL',
    `role`       VARCHAR(16)  NOT NULL DEFAULT 'CUSTOMER' COMMENT '角色: CUSTOMER/MERCHANT',
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0禁用',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone_role` (`phone`, `role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 商家表
CREATE TABLE IF NOT EXISTS `merchant` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL COMMENT '关联 user.id',
    `name`          VARCHAR(64)  NOT NULL COMMENT '店铺名称',
    `logo`          VARCHAR(256) DEFAULT '' COMMENT 'Logo URL',
    `phone`         VARCHAR(20)  DEFAULT '' COMMENT '店铺电话',
    `address`       VARCHAR(256) DEFAULT '' COMMENT '店铺地址',
    `description`   VARCHAR(512) DEFAULT '' COMMENT '店铺简介',
    `opening_hours` VARCHAR(64)  DEFAULT '09:00-21:00' COMMENT '营业时间',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1营业 0歇业',
    `min_amount`    DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '起送价',
    `delivery_fee`  DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '配送费',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- 3. 分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `merchant_id` BIGINT      NOT NULL COMMENT '所属商家',
    `name`        VARCHAR(32) NOT NULL COMMENT '分类名称',
    `sort_order`  INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- 4. 菜品表
CREATE TABLE IF NOT EXISTS `dish` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `merchant_id` BIGINT        NOT NULL COMMENT '所属商家',
    `category_id` BIGINT        NOT NULL COMMENT '所属分类',
    `name`        VARCHAR(64)   NOT NULL COMMENT '菜品名称',
    `image`       VARCHAR(256)  DEFAULT '' COMMENT '图片 URL',
    `price`       DECIMAL(8,2)  NOT NULL COMMENT '价格',
    `description` VARCHAR(256)  DEFAULT '' COMMENT '描述',
    `sales_count` INT           NOT NULL DEFAULT 0 COMMENT '销量',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 1上架 0下架',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_merchant_category` (`merchant_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 5. 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `order_no`      VARCHAR(32)   NOT NULL COMMENT '订单号',
    `user_id`       BIGINT        NOT NULL COMMENT '下单用户',
    `merchant_id`   BIGINT        NOT NULL COMMENT '所属商家',
    `address_snap`  JSON          NOT NULL COMMENT '地址快照',
    `total_amount`  DECIMAL(8,2)  NOT NULL COMMENT '菜品总额',
    `delivery_fee`  DECIMAL(8,2)  NOT NULL DEFAULT 0.00 COMMENT '配送费',
    `pay_amount`    DECIMAL(8,2)  NOT NULL COMMENT '实付金额',
    `status`        VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACCEPTED/REJECTED/DELIVERING/COMPLETED/CANCELLED',
    `remark`        VARCHAR(256)  DEFAULT '' COMMENT '备注',
    `pay_time`      DATETIME      DEFAULT NULL COMMENT '支付时间',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_merchant_status` (`merchant_id`, `status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 6. 订单明细表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `order_id`   BIGINT       NOT NULL COMMENT '订单 ID',
    `dish_id`    BIGINT       NOT NULL COMMENT '菜品 ID',
    `dish_name`  VARCHAR(64)  NOT NULL COMMENT '菜品名称快照',
    `dish_image` VARCHAR(256) DEFAULT '' COMMENT '图片快照',
    `price`      DECIMAL(8,2) NOT NULL COMMENT '单价快照',
    `quantity`   INT          NOT NULL COMMENT '数量',
    `amount`     DECIMAL(8,2) NOT NULL COMMENT '小计',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 7. 地址表
CREATE TABLE IF NOT EXISTS `address` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT       NOT NULL COMMENT '用户 ID',
    `contact_name` VARCHAR(32)  NOT NULL COMMENT '联系人',
    `phone`        VARCHAR(11)  NOT NULL COMMENT '联系电话',
    `province`     VARCHAR(32)  NOT NULL COMMENT '省',
    `city`         VARCHAR(32)  NOT NULL COMMENT '市',
    `district`     VARCHAR(32)  NOT NULL COMMENT '区',
    `detail`       VARCHAR(128) NOT NULL COMMENT '详细地址',
    `is_default`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认: 1是 0否',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址表';
