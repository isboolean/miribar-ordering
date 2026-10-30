-- MiriBar 在线扫码点餐系统 - 数据库初始化脚本
-- 使用 MySQL

CREATE DATABASE IF NOT EXISTS miribar_ordering_single DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE miribar_ordering_single;

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
    password VARCHAR(100) COMMENT '密码(加盐SHA-256，手机号验证码登录可不设)',
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

INSERT INTO t_staff (id, store_id, username, password, phone, real_name, role, status) VALUES
(1, 1, 'admin', 'sha256$76960a2c2138f38d$85a1ada6a9b75c3bbf80aceddc22342a12ac28670ae2ffb35bb91dc318dfc819', '13800000001', '管理员', 3, 1),
(2, 1, 'bar01', 'sha256$3cdc24f3aaadddce$6f2d832f91a6c0617afb83e54b94ca3fd39ee7821958e5da0f7ee5ba14a190a4', '13800000002', '张调酒师', 0, 1),
(3, 1, 'kitchen01', 'sha256$8a2239cb5c1dcef6$fa6a54583ea196b9f8b58312f27d835df4f54382254ca8923423620bbf8ed6eb', '13800000003', '李厨师', 1, 1),
(4, 1, 'manager01', 'sha256$61d64eba5355ecbc$7aa721e107e16b609c982a3affdfc14b857bb36825af99ce9852a59e086d71bb', '13800000004', '王店长', 2, 1);

-- ============================================
-- 权限管理系统表结构（第一阶段）
-- ============================================

-- 菜单权限表
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

-- 角色表
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

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS t_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id)
) COMMENT '角色菜单关联表';

-- 员工角色关联表
CREATE TABLE IF NOT EXISTS t_staff_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    staff_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_staff_role (staff_id, role_id),
    INDEX idx_staff_id (staff_id)
) COMMENT '员工角色关联表';

-- 修改 t_staff 表：增加 org_id 字段
ALTER TABLE t_staff ADD COLUMN IF NOT EXISTS org_id BIGINT COMMENT '所属组织ID';
ALTER TABLE t_staff ADD INDEX IF NOT EXISTS idx_org_id (org_id);

-- ============================================
-- 权限管理系统初始化数据
-- ============================================

-- 初始化菜单数据
-- 一级目录：员工管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order) VALUES
(1, 0, 0, '员工管理', '/staff', NULL, 'User', 1),
(2, 0, 0, '系统管理', '/system', NULL, 'Setting', 2);

-- 二级菜单：员工管理下的子菜单
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order) VALUES
(11, 1, 1, '员工列表', '/staff/list', 'staff/StaffList', 'list', 1),
(12, 1, 1, '角色管理', '/staff/role', 'staff/RoleManage', 'peoples', 2),
(13, 1, 1, '组织管理', '/staff/org', 'staff/OrgManage', 'tree', 3);

-- 二级菜单：系统管理下的子菜单
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order) VALUES
(21, 2, 1, '字典管理', '/system/dict', 'system/DictManage', 'dict', 1);

-- 三级按钮权限：员工列表页面
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order) VALUES
(111, 11, 2, '查询员工', 'staff:query', 1),
(112, 11, 2, '新增员工', 'staff:add', 2),
(113, 11, 2, '修改员工', 'staff:edit', 3),
(114, 11, 2, '删除员工', 'staff:delete', 4),
(115, 11, 2, '重置密码', 'staff:resetPwd', 5),
(116, 11, 2, '分配角色', 'staff:assignRole', 6);

-- 三级按钮权限：角色管理页面
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order) VALUES
(121, 12, 2, '查询角色', 'role:query', 1),
(122, 12, 2, '新增角色', 'role:add', 2),
(123, 12, 2, '修改角色', 'role:edit', 3),
(124, 12, 2, '删除角色', 'role:delete', 4),
(125, 12, 2, '分配权限', 'role:assignMenu', 5);

-- 三级按钮权限：组织管理页面
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order) VALUES
(131, 13, 2, '查询组织', 'org:query', 1),
(132, 13, 2, '新增组织', 'org:add', 2),
(133, 13, 2, '修改组织', 'org:edit', 3),
(134, 13, 2, '删除组织', 'org:delete', 4);

-- 三级按钮权限：字典管理页面
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order) VALUES
(211, 21, 2, '查询字典', 'dict:query', 1),
(212, 21, 2, '新增字典', 'dict:add', 2),
(213, 21, 2, '修改字典', 'dict:edit', 3),
(214, 21, 2, '删除字典', 'dict:delete', 4);

-- 初始化角色数据
INSERT INTO t_role (id, role_name, role_code, description, data_scope) VALUES
(1, '超级管理员', 'super_admin', '拥有系统所有权限', 1),
(2, '店长', 'store_manager', '管理本店所有业务', 3),
(3, '吧台员工', 'bar_staff', '吧台日常运营', 3),
(4, '后厨员工', 'kitchen_staff', '后厨日常运营', 3);

-- 为超级管理员分配所有菜单权限
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 11), (1, 12), (1, 13), (1, 21),
(1, 111), (1, 112), (1, 113), (1, 114), (1, 115), (1, 116),
(1, 121), (1, 122), (1, 123), (1, 124), (1, 125),
(1, 131), (1, 132), (1, 133), (1, 134),
(1, 211), (1, 212), (1, 213), (1, 214);

-- 为店长分配员工管理和角色管理权限
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 11), (2, 12), (2, 13),
(2, 111), (2, 112), (2, 113), (2, 115),
(2, 121), (2, 131);

-- 为吧台员工分配员工列表查询权限
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 11), (3, 111);

-- 为后厨员工分配员工列表查询权限
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(4, 1), (4, 11), (4, 111);

-- 为 admin 用户分配超级管理员角色
INSERT INTO t_staff_role (staff_id, role_id) VALUES (1, 1);

-- 为其他员工分配对应角色
INSERT INTO t_staff_role (staff_id, role_id) VALUES (2, 3), (3, 4), (4, 2);

-- ============================================
-- 组织管理表结构（第二阶段）
-- ============================================

-- 组织表
CREATE TABLE IF NOT EXISTS t_org (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父组织ID（0=顶级）',
    org_name VARCHAR(100) NOT NULL COMMENT '组织名称',
    org_code VARCHAR(50) NOT NULL UNIQUE COMMENT '组织编码',
    org_type TINYINT NOT NULL COMMENT '组织类型 1=集团 2=省份 3=城市 4=店面',
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

-- 修改 t_store 表：增加 org_id 关联
ALTER TABLE t_store ADD COLUMN IF NOT EXISTS org_id BIGINT COMMENT '所属组织ID';
ALTER TABLE t_store ADD INDEX IF NOT EXISTS idx_org_id (org_id);

-- ============================================
-- 组织管理初始化数据
-- ============================================

-- 初始化示例组织架构
INSERT INTO t_org (id, parent_id, org_name, org_code, org_type, leader, phone, address, sort_order) VALUES
(1, 0, 'MiriBar 集团', 'miribar', 1, '张总', '13800000000', '北京市朝阳区', 1),
(2, 1, '北京市', 'beijing', 2, '李经理', '13800000001', '北京市', 1),
(3, 2, '朝阳区', 'chaoyang', 3, '王主管', '13800000002', '朝阳区', 1),
(4, 3, 'MiriBar 旗舰店', 'flagship', 4, '赵店长', '13800000003', '北京市朝阳区建国路88号', 1);

-- 更新门店的组织关联
UPDATE t_store SET org_id = 4 WHERE id = 1;

-- 更新员工的组织关联
UPDATE t_staff SET org_id = 4 WHERE id IN (1, 2, 3, 4);

-- ============================================
-- 字典管理表结构（第三阶段）
-- ============================================

-- 字典类型表
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

-- 字典数据表
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

-- ============================================
-- 字典管理初始化数据
-- ============================================

-- 初始化字典类型
INSERT INTO t_dict_type (dict_name, dict_type, remark, status) VALUES
('员工初始密码', 'staff_init_password', '员工重置密码时使用的默认密码', 1),
('订单状态', 'order_status', '订单的状态枚举', 1),
('支付方式', 'payment_method', '支付的方式枚举', 1),
('组织类型', 'org_type', '组织的类型枚举', 1),
('菜单类型', 'menu_type', '菜单的类型枚举', 1),
('数据范围', 'data_scope', '角色的数据权限范围', 1);

-- 初始化字典数据
-- 员工初始密码
INSERT INTO t_dict_data (dict_type, dict_label, dict_value, is_default, sort_order) VALUES
('staff_init_password', '123456', '123456', 1, 1);

-- 订单状态
INSERT INTO t_dict_data (dict_type, dict_label, dict_value, css_class, sort_order) VALUES
('order_status', '待支付', '0', 'warning', 1),
('order_status', '已支付', '1', 'success', 2),
('order_status', '制作中', '2', 'primary', 3),
('order_status', '已完成', '3', 'info', 4),
('order_status', '已取消', '4', 'danger', 5);

-- 支付方式
INSERT INTO t_dict_data (dict_type, dict_label, dict_value, sort_order) VALUES
('payment_method', '现金', 'cash', 1),
('payment_method', '微信', 'wechat', 2),
('payment_method', '支付宝', 'alipay', 3),
('payment_method', '银行卡', 'bank', 4);

-- 组织类型
INSERT INTO t_dict_data (dict_type, dict_label, dict_value, sort_order) VALUES
('org_type', '集团', '1', 1),
('org_type', '省份', '2', 2),
('org_type', '城市', '3', 3),
('org_type', '店面', '4', 4);

-- 菜单类型
INSERT INTO t_dict_data (dict_type, dict_label, dict_value, sort_order) VALUES
('menu_type', '目录', '0', 1),
('menu_type', '菜单', '1', 2),
('menu_type', '按钮', '2', 3);

-- 数据范围
INSERT INTO t_dict_data (dict_type, dict_label, dict_value, remark, sort_order) VALUES
('data_scope', '全部数据', '1', '可以查看所有数据', 1),
('data_scope', '本组织及下级', '2', '可以查看本组织及下级组织数据', 2),
('data_scope', '仅本组织', '3', '只能查看本组织数据', 3);
