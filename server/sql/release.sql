-- ============================================================
-- MiriBar 在线扫码点餐系统 - 版本上线统一初始化脚本
-- 目标数据库：MySQL 8.0+（utf8mb4）
--
-- 本文件由以下脚本整理合并而来（按执行顺序）：
--   schema.sql                 表结构 + 基础业务数据
--   update_permission.sql      权限/菜单/角色数据（已修正语法错误、剔除与新版冲突的旧组织树）
--   seed_demo_org_staff.sql    多门店演示数据（幂等，纯生产环境可整段跳过「第八部分」）
--
-- 已剔除内容：
--   1) MariaDB 专属语法 ALTER TABLE ... ADD COLUMN IF NOT EXISTS（org_id 已内联到建表语句）
--   2) update_permission.sql 第二阶段的旧版拼音编码组织树（与新版 10 位数字编码冲突）
--   3) 存量库迁移脚本 migrate_org_code / migrate_org_hq_division / migrate_staff_no
--      （仅老库升级使用，全新安装无需执行）
-- ============================================================

SET NAMES utf8mb4;

-- ============================================================
-- 一、数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS miribar_ordering_single DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE miribar_ordering_single;

-- ============================================================
-- 二、表结构
-- ============================================================

-- 2.1 门店表
CREATE TABLE IF NOT EXISTS t_store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '门店名称',
    address VARCHAR(255) COMMENT '门店地址',
    business_hours VARCHAR(100) COMMENT '营业时段 如 18:00-02:00',
    payment_mode TINYINT DEFAULT 0 COMMENT '支付模式 0-线下结账 1-在线支付',
    org_id BIGINT COMMENT '所属组织ID',
    status TINYINT DEFAULT 1 COMMENT '状态 0-停业 1-营业中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_org_id (org_id)
) COMMENT '门店';

-- 2.2 桌台表
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

-- 2.3 商品分类表
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

-- 2.4 商品表
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

-- 2.5 规格表 (SKU)
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

-- 2.6 顾客表
CREATE TABLE IF NOT EXISTS t_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(64) COMMENT '微信openid',
    phone VARCHAR(20) COMMENT '手机号',
    password VARCHAR(100) COMMENT '密码(加盐SHA-256，手机号验证码登录可不设)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(500) COMMENT '头像',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_openid (openid),
    INDEX idx_phone (phone)
) COMMENT '顾客';

-- 2.7 订单表
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

-- 2.8 订单项表
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

-- 2.9 员工表
CREATE TABLE IF NOT EXISTS t_staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL COMMENT '所属门店',
    username VARCHAR(50) NOT NULL COMMENT '账号',
    password VARCHAR(100) NOT NULL COMMENT '密码(加密)',
    phone VARCHAR(20) COMMENT '手机号(短信登录)',
    real_name VARCHAR(50) COMMENT '姓名',
    role TINYINT DEFAULT 0 COMMENT '角色 0-吧台员工 1-后厨员工 2-店长 3-平台管理员',
    org_id BIGINT COMMENT '所属组织ID',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_store_id (store_id),
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_org_id (org_id)
) COMMENT '员工';

-- 2.10 菜单权限表
CREATE TABLE IF NOT EXISTS t_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID（0=顶级）',
    menu_type TINYINT NOT NULL COMMENT '类型：0=目录 1=菜单 2=按钮/权限',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) COMMENT '路由路径（菜单类型）',
    component VARCHAR(200) COMMENT '前端组件路径',
    permission_code VARCHAR(100) COMMENT '权限编码（按钮类型，如 staff:add）',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见 0-隐藏 1-显示',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
) COMMENT '菜单权限表';

-- 2.11 角色表
CREATE TABLE IF NOT EXISTS t_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码（如 admin）',
    description VARCHAR(200) COMMENT '描述',
    data_scope TINYINT DEFAULT 1 COMMENT '数据范围 1=全部 2=本组织及下级 3=仅本组织',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_code (role_code)
) COMMENT '角色表';

-- 2.12 角色菜单关联表
CREATE TABLE IF NOT EXISTS t_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id)
) COMMENT '角色菜单关联表';

-- 2.13 员工角色关联表
CREATE TABLE IF NOT EXISTS t_staff_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    staff_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_staff_role (staff_id, role_id),
    INDEX idx_staff_id (staff_id)
) COMMENT '员工角色关联表';

-- 2.14 组织表
CREATE TABLE IF NOT EXISTS t_org (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父组织ID（0=顶级）',
    org_name VARCHAR(100) NOT NULL COMMENT '组织名称',
    org_code VARCHAR(10) NOT NULL UNIQUE COMMENT '组织编码（10位数字，系统自动生成）',
    org_type TINYINT NOT NULL COMMENT '组织类型 1=集团 2=总部 3=分部 4=区域 5=总部机构 6=省份 7=城市 8=店面',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(255) COMMENT '地址',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_org_code (org_code)
) COMMENT '组织表';

-- 2.15 字典类型表
CREATE TABLE IF NOT EXISTS t_dict_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(100) NOT NULL UNIQUE COMMENT '字典类型（唯一标识）',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dict_type (dict_type)
) COMMENT '字典类型表';

-- 2.16 字典数据表
CREATE TABLE IF NOT EXISTS t_dict_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典键值',
    css_class VARCHAR(100) COMMENT '样式属性',
    list_class VARCHAR(100) COMMENT '表格回显样式',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认 0-否 1-是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dict_type (dict_type)
) COMMENT '字典数据表';

-- ============================================================
-- 三、基础业务种子数据
-- ============================================================

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

-- 内置员工账号：admin/admin123，其余 123456
INSERT INTO t_staff (id, store_id, username, password, phone, real_name, role, status) VALUES
(1, 1, 'admin', 'sha256$76960a2c2138f38d$85a1ada6a9b75c3bbf80aceddc22342a12ac28670ae2ffb35bb91dc318dfc819', '13800000001', '管理员', 3, 1),
(2, 1, 'bar01', 'sha256$3cdc24f3aaadddce$6f2d832f91a6c0617afb83e54b94ca3fd39ee7821958e5da0f7ee5ba14a190a4', '13800000002', '张调酒师', 0, 1),
(3, 1, 'kitchen01', 'sha256$8a2239cb5c1dcef6$fa6a54583ea196b9f8b58312f27d835df4f54382254ca8923423620bbf8ed6eb', '13800000003', '李厨师', 1, 1),
(4, 1, 'manager01', 'sha256$61d64eba5355ecbc$7aa721e107e16b609c982a3affdfc14b857bb36825af99ce9852a59e086d71bb', '13800000004', '王店长', 2, 1);

-- ============================================================
-- 四、权限数据：菜单全集（含 菜单管理 / 业务管理 / 客户管理）
-- ============================================================

-- 4.1 一级目录
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order, visible) VALUES
(1, 0, 0, '员工管理', '/staff', NULL, 'User', 1, 1),
(2, 0, 0, '系统管理', '/system', NULL, 'Setting', 2, 1),
(3, 0, 0, '业务管理', '/business', NULL, 'ShoppingCart', 3, 1),
(4, 0, 0, '客户管理', '/customer', NULL, 'UserGroup', 4, 1);

-- 4.2 二级菜单
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order, visible) VALUES
(11, 1, 1, '员工列表', '/staff/list', 'staff/StaffList', 'list', 1, 1),
(12, 1, 1, '角色管理', '/staff/role', 'staff/RoleManage', 'peoples', 2, 1),
(13, 1, 1, '组织管理', '/staff/org', 'staff/OrgManage', 'tree', 3, 1),
(21, 2, 1, '菜单管理', '/system/menu', 'system/MenuManage', 'menu', 1, 1),
(22, 2, 1, '字典管理', '/system/dict', 'system/DictManage', 'dict', 2, 1),
(31, 3, 1, '桌台管理', '/business/tables', 'business/Tables', 'table', 1, 1),
(32, 3, 1, '分类管理', '/business/categories', 'business/Categories', 'folder', 2, 1),
(33, 3, 1, '商品管理', '/business/products', 'business/Products', 'box', 3, 1),
(34, 3, 1, '结账管理', '/business/checkout', 'business/Checkout', 'money', 4, 1),
(41, 4, 1, '客户列表', '/customer/list', 'customer/CustomerList', 'list', 1, 1);

-- 4.3 三级按钮权限：员工列表
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(111, 11, 2, '查询员工', 'staff:query', 1, 1),
(112, 11, 2, '新增员工', 'staff:add', 2, 1),
(113, 11, 2, '修改员工', 'staff:edit', 3, 1),
(114, 11, 2, '删除员工', 'staff:delete', 4, 1),
(115, 11, 2, '重置密码', 'staff:resetPwd', 5, 1),
(116, 11, 2, '分配角色', 'staff:assignRole', 6, 1);

-- 4.4 三级按钮权限：角色管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(121, 12, 2, '查询角色', 'role:query', 1, 1),
(122, 12, 2, '新增角色', 'role:add', 2, 1),
(123, 12, 2, '修改角色', 'role:edit', 3, 1),
(124, 12, 2, '删除角色', 'role:delete', 4, 1),
(125, 12, 2, '分配权限', 'role:assignMenu', 5, 1);

-- 4.5 三级按钮权限：组织管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(131, 13, 2, '查询组织', 'org:query', 1, 1),
(132, 13, 2, '新增组织', 'org:add', 2, 1),
(133, 13, 2, '修改组织', 'org:edit', 3, 1),
(134, 13, 2, '删除组织', 'org:delete', 4, 1);

-- 4.6 三级按钮权限：菜单管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(211, 21, 2, '查询菜单', 'menu:query', 1, 1),
(212, 21, 2, '新增菜单', 'menu:add', 2, 1),
(213, 21, 2, '修改菜单', 'menu:edit', 3, 1),
(214, 21, 2, '删除菜单', 'menu:delete', 4, 1);

-- 4.7 三级按钮权限：字典管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(221, 22, 2, '查询字典', 'dict:query', 1, 1),
(222, 22, 2, '新增字典', 'dict:add', 2, 1),
(223, 22, 2, '修改字典', 'dict:edit', 3, 1),
(224, 22, 2, '删除字典', 'dict:delete', 4, 1);

-- 4.8 三级按钮权限：客户列表
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(411, 41, 2, '查询客户', 'customer:query', 1, 1),
(412, 41, 2, '新增客户', 'customer:add', 2, 1),
(413, 41, 2, '修改客户', 'customer:edit', 3, 1),
(414, 41, 2, '删除客户', 'customer:delete', 4, 1);

-- ============================================================
-- 五、权限数据：角色与授权
-- ============================================================

INSERT INTO t_role (id, role_name, role_code, description, data_scope) VALUES
(1, '超级管理员', 'super_admin', '拥有系统所有权限', 1),
(2, '店长', 'store_manager', '管理本店所有业务', 3),
(3, '吧台员工', 'bar_staff', '吧台日常运营', 3),
(4, '后厨员工', 'kitchen_staff', '后厨日常运营', 3);

-- 超级管理员：所有菜单 + 所有按钮权限
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 11), (1, 12), (1, 13), (1, 21), (1, 22),
(1, 31), (1, 32), (1, 33), (1, 34), (1, 41),
(1, 111), (1, 112), (1, 113), (1, 114), (1, 115), (1, 116),
(1, 121), (1, 122), (1, 123), (1, 124), (1, 125),
(1, 131), (1, 132), (1, 133), (1, 134),
(1, 211), (1, 212), (1, 213), (1, 214),
(1, 221), (1, 222), (1, 223), (1, 224),
(1, 411), (1, 412), (1, 413), (1, 414);

-- 店长：员工管理 + 组织查询
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 11), (2, 12), (2, 13),
(2, 111), (2, 112), (2, 113), (2, 115),
(2, 121), (2, 131);

-- 吧台员工：员工列表查询
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 11), (3, 111);

-- 后厨员工：员工列表查询
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(4, 1), (4, 11), (4, 111);

-- 员工-角色关联：1=超级管理员 2=店长 3=吧台 4=后厨
INSERT INTO t_staff_role (staff_id, role_id) VALUES
(1, 1), (2, 3), (3, 4), (4, 2);

-- ============================================================
-- 六、组织架构（新版 10 位数字编码）
--    层级：集团 -> 总部 / 区域分部 -> 省份 -> 城市 -> 店面
--    类型：1=集团 2=总部 3=分部 4=区域 5=总部机构 6=省份 7=城市 8=店面
-- ============================================================

INSERT INTO t_org (id, parent_id, org_name, org_code, org_type, leader, phone, address, sort_order) VALUES
(1,  0,  'MiriBar 集团',      '0000000001', 1, '张总',   '13800000000', '北京市朝阳区', 1),
(2,  1,  '总部',              '0000000024', 2, '张总',   '13800000150', '北京市朝阳区建国路88号 MiriBar 大厦', 1),
(3,  2,  '运营中心',          '0000000025', 5, '周运营', '13800000151', '北京市朝阳区建国路88号 8F', 1),
(4,  2,  '财务中心',          '0000000026', 5, '陈财务', '13800000152', '北京市朝阳区建国路88号 8F', 2),
(5,  2,  '人力资源中心',      '0000000027', 5, '吴人事', '13800000153', '北京市朝阳区建国路88号 9F', 3),
(6,  2,  '技术中心',          '0000000028', 5, '林技术', '13800000154', '北京市朝阳区建国路88号 9F', 4),
(7,  2,  '供应链中心',        '0000000029', 5, '黄供应', '13800000155', '北京市朝阳区建国路88号 10F', 5),
(8,  2,  '市场中心',          '0000000030', 5, '孙市场', '13800000156', '北京市朝阳区建国路88号 10F', 6),
(9,  1,  '华北分部',          '0000000031', 3, '刘京生', '13800000157', '北京市', 2),
(10, 9,  '北京市',            '0000000002', 6, '李经理', '13800000001', '北京市', 1),
(11, 10, '朝阳区',            '0000000003', 7, '王主管', '13800000002', '朝阳区', 1),
(12, 11, 'MiriBar 旗舰店',    '0000000004', 8, '赵店长', '13800000003', '北京市朝阳区建国路88号', 1);

-- 门店与员工的组织关联（旗舰店挂到店面节点）
UPDATE t_store SET org_id = 12 WHERE id = 1;
UPDATE t_staff SET org_id = 12 WHERE id IN (1, 2, 3, 4);

-- ============================================================
-- 七、字典数据
-- ============================================================

INSERT INTO t_dict_type (dict_name, dict_type, remark, status) VALUES
('员工初始密码', 'staff_init_password', '员工重置密码时使用的默认密码', 1),
('订单状态', 'order_status', '订单的状态枚举', 1),
('支付方式', 'payment_method', '支付的方式枚举', 1),
('组织类型', 'org_type', '组织的类型枚举', 1),
('菜单类型', 'menu_type', '菜单的类型枚举', 1),
('数据范围', 'data_scope', '角色的数据权限范围', 1);

INSERT INTO t_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES
('staff_init_password', '123456', '123456', 1, 1);

INSERT INTO t_dict_data (dict_type, dict_label, dict_value, css_class, sort_order) VALUES
('order_status', '待支付', '0', 'warning', 1),
('order_status', '已支付', '1', 'success', 2),
('order_status', '制作中', '2', 'primary', 3),
('order_status', '已完成', '3', 'info', 4),
('order_status', '已取消', '4', 'danger', 5);

INSERT INTO t_dict_data (dict_type, dict_label, dict_value, sort_order) VALUES
('payment_method', '现金', 'cash', 1),
('payment_method', '微信', 'wechat', 2),
('payment_method', '支付宝', 'alipay', 3),
('payment_method', '银行卡', 'bank', 4);

INSERT INTO t_dict_data (dict_type, dict_label, dict_value, sort_order) VALUES
('org_type', '集团', '1', 1),
('org_type', '省份', '2', 2),
('org_type', '城市', '3', 3),
('org_type', '店面', '4', 4);

INSERT INTO t_dict_data (dict_type, dict_label, dict_value, sort_order) VALUES
('menu_type', '目录', '0', 1),
('menu_type', '菜单', '1', 2),
('menu_type', '按钮', '2', 3);

INSERT INTO t_dict_data (dict_type, dict_label, dict_value, remark, sort_order) VALUES
('data_scope', '全部数据', '1', '可以查看所有数据', 1),
('data_scope', '本组织及下级', '2', '可以查看本组织及下级组织数据', 2),
('data_scope', '仅本组织', '3', '只能查看本组织数据', 3);

-- ============================================================
-- 八、多门店演示数据（幂等，可重复执行；纯生产环境可跳过本段）
--    层级：集团 -> 总部/分部 -> 省 -> 市 -> 店面，含 6 家演示门店与员工
-- ============================================================

-- 8.1 总部与区域分部（与第六部分按 org_code 幂等去重）
INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000001' parent_code, '总部' org_name, '0000000024' org_code, 2 org_type, '张总' leader, '13800000150' phone, '北京市朝阳区建国路88号 MiriBar 大厦' address, 1 sort_order
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000024' parent_code, '运营中心' org_name, '0000000025' org_code, 5 org_type, '周运营' leader, '13800000151' phone, '北京市朝阳区建国路88号 8F' address, 1 sort_order
    UNION ALL SELECT '0000000024', '财务中心',     '0000000026', 5, '陈财务', '13800000152', '北京市朝阳区建国路88号 8F', 2
    UNION ALL SELECT '0000000024', '人力资源中心', '0000000027', 5, '吴人事', '13800000153', '北京市朝阳区建国路88号 9F', 3
    UNION ALL SELECT '0000000024', '技术中心',     '0000000028', 5, '林技术', '13800000154', '北京市朝阳区建国路88号 9F', 4
    UNION ALL SELECT '0000000024', '供应链中心',   '0000000029', 5, '黄供应', '13800000155', '北京市朝阳区建国路88号 10F', 5
    UNION ALL SELECT '0000000024', '市场中心',     '0000000030', 5, '孙市场', '13800000156', '北京市朝阳区建国路88号 10F', 6
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000001' parent_code, '华北分部' org_name, '0000000031' org_code, 3 org_type, '刘京生' leader, '13800000157' phone, '北京市' address, 2 sort_order
    UNION ALL SELECT '0000000001', '华东分部', '0000000032', 3, '钱沪生', '13800000158', '上海市', 3
    UNION ALL SELECT '0000000001', '华南分部', '0000000033', 3, '梁粤',   '13800000159', '广东省', 4
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

-- 8.2 省份（挂对应分部）
INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000032' parent_code, '上海市' org_name, '0000000005' org_code, 6 org_type, '钱沪生' leader, '13800000101' phone, '上海市' address, 1 sort_order
    UNION ALL SELECT '0000000033', '广东省', '0000000006', 6, '梁粤', '13800000102', '广东省', 1
    UNION ALL SELECT '0000000032', '浙江省', '0000000007', 6, '苏杭', '13800000103', '浙江省', 2
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

-- 8.3 城市
INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000002' parent_code, '海淀区' org_name, '0000000008' org_code, 7 org_type, '孙海' leader, '13800000104' phone, '北京市海淀区' address, 2 sort_order
    UNION ALL SELECT '0000000002', '通州区', '0000000009', 7, '郑通州', '13800000105', '北京市通州区', 3
    UNION ALL SELECT '0000000005', '浦东新区', '0000000010', 7, '何浦东', '13800000106', '上海市浦东新区', 1
    UNION ALL SELECT '0000000005', '徐汇区', '0000000011', 7, '沈徐汇', '13800000107', '上海市徐汇区', 2
    UNION ALL SELECT '0000000005', '静安区', '0000000012', 7, '韩静安', '13800000108', '上海市静安区', 3
    UNION ALL SELECT '0000000006', '天河区', '0000000013', 7, '黄天河', '13800000109', '广州市天河区', 1
    UNION ALL SELECT '0000000006', '南山区', '0000000014', 7, '唐南山', '13800000110', '深圳市南山区', 2
    UNION ALL SELECT '0000000006', '福田区', '0000000015', 7, '邓福田', '13800000111', '深圳市福田区', 3
    UNION ALL SELECT '0000000007', '西湖区', '0000000016', 7, '叶西湖', '13800000112', '杭州市西湖区', 1
    UNION ALL SELECT '0000000007', '滨江区', '0000000017', 7, '江水', '13800000113', '杭州市滨江区', 2
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

-- 8.4 店面
INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000008' parent_code, 'MiriBar 海淀店' org_name, '0000000018' org_code, 8 org_type, '孙海' leader, '13800000114' phone, '北京市海淀区中关村大街1号' address, 1 sort_order
    UNION ALL SELECT '0000000010', 'MiriBar 浦东店', '0000000019', 8, '何浦东', '13800000115', '上海市浦东新区世纪大道100号', 1
    UNION ALL SELECT '0000000011', 'MiriBar 徐汇店', '0000000020', 8, '沈徐汇', '13800000116', '上海市徐汇区淮海中路300号', 1
    UNION ALL SELECT '0000000013', 'MiriBar 天河店', '0000000021', 8, '黄天河', '13800000117', '广州市天河区天河路200号', 1
    UNION ALL SELECT '0000000014', 'MiriBar 南山店', '0000000022', 8, '唐南山', '13800000118', '深圳市南山区科技园南路8号', 1
    UNION ALL SELECT '0000000016', 'MiriBar 西湖店', '0000000023', 8, '叶西湖', '13800000119', '杭州市西湖区文三路50号', 1
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

-- 8.5 演示门店（每个店面机构一家）
INSERT INTO t_store (name, address, business_hours, payment_mode, status, org_id)
SELECT v.name, v.address, '18:00-02:00', 0, 1, o.id
FROM (
    SELECT '0000000018' org_code, 'MiriBar 海淀店' name, '北京市海淀区中关村大街1号' address
    UNION ALL SELECT '0000000019', 'MiriBar 浦东店', '上海市浦东新区世纪大道100号'
    UNION ALL SELECT '0000000020', 'MiriBar 徐汇店', '上海市徐汇区淮海中路300号'
    UNION ALL SELECT '0000000021', 'MiriBar 天河店', '广州市天河区天河路200号'
    UNION ALL SELECT '0000000022', 'MiriBar 南山店', '深圳市南山区科技园南路8号'
    UNION ALL SELECT '0000000023', 'MiriBar 西湖店', '杭州市西湖区文三路50号'
) v
JOIN t_org o ON o.org_code = v.org_code
WHERE NOT EXISTS (SELECT 1 FROM t_store s WHERE s.org_id = o.id);

-- 8.6 演示桌台（每店 3 张）
INSERT INTO t_dining_table (store_id, table_number, qr_token, seats, status)
SELECT s.id, v.table_number, v.qr_token, v.seats, 0
FROM (
    SELECT '0000000018' org_code, 'A01' table_number, 'tbl_hd_a01' qr_token, 4 seats
    UNION ALL SELECT '0000000018', 'A02', 'tbl_hd_a02', 4
    UNION ALL SELECT '0000000018', 'B01', 'tbl_hd_b01', 6
    UNION ALL SELECT '0000000019', 'A01', 'tbl_pd_a01', 4
    UNION ALL SELECT '0000000019', 'A02', 'tbl_pd_a02', 4
    UNION ALL SELECT '0000000019', 'B01', 'tbl_pd_b01', 6
    UNION ALL SELECT '0000000020', 'A01', 'tbl_xh_a01', 4
    UNION ALL SELECT '0000000020', 'A02', 'tbl_xh_a02', 2
    UNION ALL SELECT '0000000020', 'B01', 'tbl_xh_b01', 6
    UNION ALL SELECT '0000000021', 'A01', 'tbl_th_a01', 4
    UNION ALL SELECT '0000000021', 'A02', 'tbl_th_a02', 4
    UNION ALL SELECT '0000000021', 'B01', 'tbl_th_b01', 8
    UNION ALL SELECT '0000000022', 'A01', 'tbl_ns_a01', 4
    UNION ALL SELECT '0000000022', 'A02', 'tbl_ns_a02', 4
    UNION ALL SELECT '0000000022', 'B01', 'tbl_ns_b01', 6
    UNION ALL SELECT '0000000023', 'A01', 'tbl_xihu_a01', 4
    UNION ALL SELECT '0000000023', 'A02', 'tbl_xihu_a02', 2
    UNION ALL SELECT '0000000023', 'B01', 'tbl_xihu_b01', 6
) v
JOIN t_org o ON o.org_code = v.org_code
JOIN t_store s ON s.org_id = o.id
WHERE NOT EXISTS (SELECT 1 FROM t_dining_table t WHERE t.qr_token = v.qr_token);

-- 8.7 演示员工（账号=10位工号，手机号去重；密码同 admin：admin123）
DROP TEMPORARY TABLE IF EXISTS tmp_seed_staff;
CREATE TEMPORARY TABLE tmp_seed_staff (
    org_code   CHAR(10) NOT NULL,
    store_org  CHAR(10) NOT NULL,
    emp_no     CHAR(10) NOT NULL,
    phone      VARCHAR(20) NOT NULL,
    real_name  VARCHAR(50) NOT NULL,
    role       TINYINT NOT NULL
);

INSERT INTO tmp_seed_staff (org_code, store_org, emp_no, phone, real_name, role) VALUES
('0000000025', '0000000004', '0000000004', '13800000120', '周运营',   3),
('0000000027', '0000000004', '0000000005', '13800000121', '吴人事',   3),
('0000000002', '0000000004', '0000000006', '13800000122', '刘京生',   2),
('0000000004', '0000000004', '0000000007', '13800000123', '陈雪',     0),
('0000000004', '0000000004', '0000000008', '13800000124', '周厨',     1),
('0000000009', '0000000004', '0000000009', '13800000125', '郑通州',   2),
('0000000018', '0000000018', '0000000010', '13800000126', '孙海',     2),
('0000000018', '0000000018', '0000000011', '13800000127', '马小北',   0),
('0000000018', '0000000018', '0000000012', '13800000128', '赵厨',     1),
('0000000005', '0000000019', '0000000013', '13800000129', '钱沪生',   2),
('0000000019', '0000000019', '0000000014', '13800000130', '何浦东',   2),
('0000000019', '0000000019', '0000000015', '13800000131', '林晓',     0),
('0000000019', '0000000019', '0000000016', '13800000132', '徐蕾',     0),
('0000000019', '0000000019', '0000000017', '13800000133', '高厨',     1),
('0000000020', '0000000020', '0000000018', '13800000134', '沈徐汇',   2),
('0000000020', '0000000020', '0000000019', '13800000135', '蒋甜',     0),
('0000000012', '0000000020', '0000000020', '13800000136', '韩静安',   2),
('0000000006', '0000000021', '0000000021', '13800000137', '梁粤',     2),
('0000000021', '0000000021', '0000000022', '13800000138', '黄天河',   2),
('0000000021', '0000000021', '0000000023', '13800000139', '温子豪',   0),
('0000000021', '0000000021', '0000000024', '13800000140', '罗厨',     1),
('0000000022', '0000000022', '0000000025', '13800000141', '唐南山',   2),
('0000000022', '0000000022', '0000000026', '13800000142', '曾小南',   0),
('0000000022', '0000000022', '0000000027', '13800000143', '彭厨',     1),
('0000000015', '0000000021', '0000000028', '13800000144', '邓福田',   2),
('0000000007', '0000000023', '0000000029', '13800000145', '苏杭',     2),
('0000000023', '0000000023', '0000000030', '13800000146', '叶西湖',   2),
('0000000023', '0000000023', '0000000031', '13800000147', '方雨',     0),
('0000000017', '0000000023', '0000000032', '13800000148', '江水',     2);

INSERT INTO t_staff (store_id, org_id, username, password, phone, real_name, role, status)
SELECT s.id, o.id, t.emp_no,
       'sha256$a1b2c3d4e5f60718$85cc99c527a2588b8d387b5a7f75008ac9561738dc9e18d1b9e74699e1718bbf',
       t.phone, t.real_name, t.role, 1
FROM tmp_seed_staff t
JOIN t_org o ON o.org_code = t.org_code
JOIN t_org so ON so.org_code = t.store_org
JOIN t_store s ON s.org_id = so.id
WHERE NOT EXISTS (SELECT 1 FROM t_staff x WHERE x.phone = t.phone);

DROP TEMPORARY TABLE tmp_seed_staff;

-- 8.8 补齐员工角色映射（0=吧台->3 1=后厨->4 2=店长->2 其他->1）
INSERT INTO t_staff_role (staff_id, role_id)
SELECT s.id, CASE s.role WHEN 0 THEN 3 WHEN 1 THEN 4 WHEN 2 THEN 2 ELSE 1 END
FROM t_staff s
WHERE NOT EXISTS (SELECT 1 FROM t_staff_role r WHERE r.staff_id = s.id);

-- ============================================================
-- 九、安装校验
-- ============================================================
SELECT COUNT(*) AS menu_count FROM t_menu;
SELECT COUNT(*) AS role_menu_count FROM t_role_menu;
SELECT COUNT(*) AS org_count FROM t_org;
SELECT COUNT(*) AS store_count FROM t_store;
SELECT COUNT(*) AS staff_count FROM t_staff;
SELECT COUNT(*) AS dict_type_count FROM t_dict_type;
