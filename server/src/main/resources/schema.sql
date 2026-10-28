-- MiriBar 在线扫码点餐系统 - 数据库初始化脚本
-- 使用 MySQL

CREATE DATABASE IF NOT EXISTS miribar_ordering DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE miribar_ordering;

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

-- 商品分类表
CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '所属门店',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(255) COMMENT '图标URL',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_store_id (store_id)
) COMMENT '商品分类';

-- 商品表
CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '所属门店',
    category_id BIGINT NOT NULL COMMENT '所属分类',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description VARCHAR(500) COMMENT '描述',
    image_url VARCHAR(500) COMMENT '图片URL',
    product_type TINYINT DEFAULT 0 COMMENT '类型 0-酒水 1-小食 2-套餐',
    tags VARCHAR(100) COMMENT '标签 热门,新品,限时,推荐',
    contains_alcohol TINYINT DEFAULT 0 COMMENT '是否含酒精 0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_store_id (store_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) COMMENT '商品';

-- 规格表 (SKU)
CREATE TABLE IF NOT EXISTS t_sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '所属商品',
    sku_name VARCHAR(50) NOT NULL COMMENT '规格名称 如 大杯/中杯/小杯',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT DEFAULT 0 COMMENT '库存',
    sold_out TINYINT DEFAULT 0 COMMENT '是否售罄 0-否 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id)
) COMMENT '商品规格';

-- 顾客表
CREATE TABLE IF NOT EXISTS t_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(64) COMMENT '微信openid',
    phone VARCHAR(20) COMMENT '手机号',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(500) COMMENT '头像',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_openid (openid),
    INDEX idx_phone (phone)
) COMMENT '顾客';

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

-- 员工表
CREATE TABLE IF NOT EXISTS t_staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '所属门店',
    username VARCHAR(50) NOT NULL COMMENT '账号',
    password VARCHAR(100) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(50) COMMENT '姓名',
    role TINYINT DEFAULT 0 COMMENT '角色 0-吧台员工 1-后厨员工 2-店长 3-平台管理员',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_store_id (store_id),
    INDEX idx_username (username)
) COMMENT '员工';

-- 插入示例数据
INSERT INTO t_store (id, name, address, business_hours, payment_mode, status) VALUES
(1, 'MiriBar 旗舰店', '北京市朝阳区建国路88号', '18:00-02:00', 0, 1);

INSERT INTO t_dining_table (id, store_id, table_number, qr_token, seats, status) VALUES
(1, 1, 'A01', 'tbl_a01_token', 4, 0),
(2, 1, 'A02', 'tbl_a02_token', 4, 0),
(3, 1, 'B01', 'tbl_b01_token', 6, 0),
(4, 1, 'B02', 'tbl_b02_token', 2, 0),
(5, 1, 'VIP01', 'tbl_vip01_token', 8, 0);

INSERT INTO t_category (id, store_id, name, icon, sort_order, status) VALUES
(1, 1, '啤酒', '🍺', 1, 1),
(2, 1, '鸡尾酒', '🍸', 2, 1),
(3, 1, '威士忌', '🥃', 3, 1),
(4, 1, '小食', '🍟', 4, 1),
(5, 1, '套餐', '🎁', 5, 1);

INSERT INTO t_product (id, store_id, category_id, name, description, image_url, product_type, tags, contains_alcohol, status, sort_order) VALUES
(1, 1, 1, '精酿IPA', '进口精酿IPA啤酒，口感醇厚', '', 0, '热门,推荐', 1, 1, 1),
(2, 1, 1, '小麦白啤', '清爽小麦白啤，适合入门', '', 0, '热门', 1, 1, 2),
(3, 1, 2, '莫吉托', '经典古巴鸡尾酒，薄荷清爽', '', 0, '热门,推荐', 1, 1, 1),
(4, 1, 2, '长岛冰茶', '经典长岛冰茶，后劲十足', '', 0, '新品', 1, 1, 2),
(5, 1, 3, '百威威士忌', '百威威士忌纯饮', '', 0, '', 1, 1, 1),
(6, 1, 4, '薯条', '香脆薯条配番茄酱', '', 1, '热门', 0, 1, 1),
(7, 1, 4, '炸鸡拼盘', '香酥炸鸡搭配蜂蜜芥末酱', '', 1, '推荐', 0, 1, 2),
(8, 1, 4, '烤鸡翅', '蜜汁烤鸡翅4只', '', 1, '', 0, 1, 3),
(9, 1, 5, '畅饮套餐', '3小时无限畅饮精选啤酒', '', 0, '热门,推荐', 1, 1, 1),
(10, 1, 5, '小食拼盘', '薯条+炸鸡+烤鸡翅组合', '', 1, '推荐', 0, 1, 2);

INSERT INTO t_sku (id, product_id, sku_name, price, stock, sold_out) VALUES
(1, 1, '杯', 38.00, 100, 0),
(2, 1, '扎(500ml)', 68.00, 50, 0),
(3, 2, '杯', 32.00, 100, 0),
(4, 2, '扎(500ml)', 58.00, 50, 0),
(5, 3, '标准杯', 48.00, 80, 0),
(6, 4, '标准杯', 58.00, 60, 0),
(7, 5, '纯饮(30ml)', 68.00, 40, 0),
(8, 5, '纯饮(50ml)', 98.00, 30, 0),
(9, 6, '标准份', 28.00, 200, 0),
(10, 6, '大份', 38.00, 100, 0),
(11, 7, '标准份', 48.00, 150, 0),
(12, 8, '4只装', 38.00, 120, 0),
(13, 9, '3小时畅饮', 128.00, 30, 0),
(14, 10, '标准份', 68.00, 80, 0);

INSERT INTO t_staff (id, store_id, username, password, real_name, role, status) VALUES
(1, 1, 'admin', 'admin123', '管理员', 3, 1),
(2, 1, 'bar01', '123456', '张调酒师', 0, 1),
(3, 1, 'kitchen01', '123456', '李厨师', 1, 1),
(4, 1, 'manager01', '123456', '王店长', 2, 1);
