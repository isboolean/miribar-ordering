-- =============================================
-- miribar_product 数据库 - 商品服务
-- 包含: 商品分类表、商品表、规格表(SKU)
-- =============================================

CREATE DATABASE IF NOT EXISTS miribar_product DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE miribar_product;

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

-- 示例数据
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
