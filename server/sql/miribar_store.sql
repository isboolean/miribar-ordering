-- =============================================
-- miribar_store 数据库 - 门店服务
-- 包含: 门店表、桌台表
-- =============================================

CREATE DATABASE IF NOT EXISTS miribar_store DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE miribar_store;

-- 门店表
CREATE TABLE IF NOT EXISTS t_store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '门店名称',
    address VARCHAR(255) COMMENT '门店地址',
    business_hours VARCHAR(100) COMMENT '营业时段 如 18:00-02:00',
    payment_mode TINYINT DEFAULT 0 COMMENT '支付模式 0-线下结账 1-在线支付',
    status TINYINT DEFAULT 1 COMMENT '状态 0-停业 1-营业中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '门店';

-- 桌台表
CREATE TABLE IF NOT EXISTS t_dining_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '所属门店',
    table_number VARCHAR(20) NOT NULL COMMENT '桌号',
    qr_token VARCHAR(64) UNIQUE COMMENT '桌码token',
    seats INT DEFAULT 4 COMMENT '座位数',
    status TINYINT DEFAULT 0 COMMENT '状态 0-空闲 1-已开台 2-已清台',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_store_id (store_id),
    INDEX idx_qr_token (qr_token)
) COMMENT '桌台';

-- 示例数据
INSERT INTO t_store (id, name, address, business_hours, payment_mode, status) VALUES
(1, 'MiriBar 旗舰店', '北京市朝阳区建国路88号', '18:00-02:00', 0, 1);

INSERT INTO t_dining_table (id, store_id, table_number, qr_token, seats, status) VALUES
(1, 1, 'A01', 'tbl_a01_token', 4, 0),
(2, 1, 'A02', 'tbl_a02_token', 4, 0),
(3, 1, 'B01', 'tbl_b01_token', 6, 0),
(4, 1, 'B02', 'tbl_b02_token', 2, 0),
(5, 1, 'VIP01', 'tbl_vip01_token', 8, 0);
