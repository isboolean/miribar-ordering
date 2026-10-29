-- =============================================
-- miribar_auth 数据库 - 认证服务
-- 包含: 员工表、顾客表
-- =============================================

CREATE DATABASE IF NOT EXISTS miribar_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE miribar_auth;

-- 员工表
CREATE TABLE IF NOT EXISTS t_staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '所属门店',
    username VARCHAR(50) NOT NULL COMMENT '账号',
    password VARCHAR(100) NOT NULL COMMENT '密码(加密)',
    phone VARCHAR(20) COMMENT '手机号(短信登录)',
    real_name VARCHAR(50) COMMENT '姓名',
    role TINYINT DEFAULT 0 COMMENT '角色 0-吧台员工 1-后厨员工 2-店长 3-平台管理员',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_store_id (store_id),
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) COMMENT '员工';

-- 顾客表
CREATE TABLE IF NOT EXISTS t_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(64) COMMENT '微信openid',
    phone VARCHAR(20) COMMENT '手机号',
    password VARCHAR(100) COMMENT '密码(加盐SHA-256)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(500) COMMENT '头像',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_openid (openid),
    INDEX idx_phone (phone)
) COMMENT '顾客';

-- 示例数据
INSERT INTO t_staff (id, store_id, username, password, phone, real_name, role, status) VALUES
(1, 1, 'admin', 'sha256$76960a2c2138f38d$85a1ada6a9b75c3bbf80aceddc22342a12ac28670ae2ffb35bb91dc318dfc819', '13800000001', '管理员', 3, 1),
(2, 1, 'bar01', 'sha256$3cdc24f3aaadddce$6f2d832f91a6c0617afb83e54b94ca3fd39ee7821958e5da0f7ee5ba14a190a4', '13800000002', '张调酒师', 0, 1),
(3, 1, 'kitchen01', 'sha256$8a2239cb5c1dcef6$fa6a54583ea196b9f8b58312f27d835df4f54382254ca8923423620bbf8ed6eb', '13800000003', '李厨师', 1, 1),
(4, 1, 'manager01', 'sha256$61d64eba5355ecbc$7aa721e107e16b609c982a3affdfc14b857bb36825af99ce9852a59e086d71bb', '13800000004', '王店长', 2, 1);
