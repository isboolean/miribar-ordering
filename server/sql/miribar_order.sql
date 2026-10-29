-- =============================================
-- miribar_order 数据库 - 订单服务
-- 包含: 订单表、订单项表
-- =============================================

CREATE DATABASE IF NOT EXISTS miribar_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE miribar_order;

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '所属门店',
    table_id BIGINT NOT NULL COMMENT '所属桌台',
    customer_id BIGINT COMMENT '顾客ID',
    order_no VARCHAR(32) UNIQUE NOT NULL COMMENT '订单编号',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待接单 1-制作中 2-已完成 3-已拒单 4-已取消 5-已结账',
    payment_method TINYINT DEFAULT 0 COMMENT '支付方式 0-线下 1-微信 2-支付宝',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态 0-未支付 1-已支付 2-已退款',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总金额',
    remark VARCHAR(500) COMMENT '整单备注',
    source TINYINT DEFAULT 0 COMMENT '来源 0-扫码点单 1-加单',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_store_id (store_id),
    INDEX idx_table_id (table_id),
    INDEX idx_status (status),
    INDEX idx_order_no (order_no)
) COMMENT '订单';

-- 订单项表
CREATE TABLE IF NOT EXISTS t_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '所属订单',
    sku_id BIGINT NOT NULL COMMENT '规格ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称(冗余)',
    sku_name VARCHAR(50) NOT NULL COMMENT '规格名称(冗余)',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    remark VARCHAR(200) COMMENT '单品备注',
    item_type TINYINT DEFAULT 0 COMMENT '类型 0-酒水 1-小食 2-套餐',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_sku_id (sku_id)
) COMMENT '订单项';
