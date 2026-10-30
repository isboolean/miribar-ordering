-- MiriBar 权限管理系统 - 增量更新脚本
-- 在已有数据库基础上执行，添加权限管理、组织管理、字典管理功能

USE miribar_ordering_single;

-- ============================================
-- 第一阶段：权限管理系统
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

-- 修改 t_staff 表：增加 org_id 字段（忽略已存在的情况）
-- MySQL 不支持 ALTER TABLE ADD COLUMN IF NOT EXISTS，用存储过程处理
DROP PROCEDURE IF EXISTS add_org_id_to_staff;
DELIMITER //
CREATE PROCEDURE add_org_id_to_staff()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'miribar_ordering_single' 
        AND TABLE_NAME = 't_staff' 
        AND COLUMN_NAME = 'org_id'
    ) THEN
        ALTER TABLE t_staff ADD COLUMN org_id BIGINT COMMENT '所属组织ID';
        ALTER TABLE t_staff ADD INDEX idx_org_id (org_id);
    END IF;
END //
DELIMITER ;
CALL add_org_id_to_staff();
DROP PROCEDURE IF EXISTS add_org_id_to_staff;

-- 初始化菜单数据（先清空，避免重复）
DELETE FROM t_menu;

-- 一级目录
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order, visible) VALUES
(1, 0, 0, '员工管理', '/staff', NULL, 'User', 1, 1),
(2, 0, 0, '系统管理', '/system', NULL, 'Setting', 2, 1);

-- 二级菜单
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order, visible) VALUES
(11, 1, 1, '员工列表', '/staff/list', 'staff/StaffList', 'list', 1, 1),
(12, 1, 1, '角色管理', '/staff/role', 'staff/RoleManage', 'peoples', 2, 1),
(13, 1, 1, '组织管理', '/staff/org', 'staff/OrgManage', 'tree', 3, 1),
(21, 2, 1, '菜单管理', '/system/menu', 'system/MenuManage', 'menu', 1, 1),
(22, 2, 1, '字典管理', '/system/dict', 'system/DictManage', 'dict', 2, 1);

-- 三级按钮权限：员工列表
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(111, 11, 2, '查询员工', 'staff:query', 1, 1),
(112, 11, 2, '新增员工', 'staff:add', 2, 1),
(113, 11, 2, '修改员工', 'staff:edit', 3, 1),
(114, 11, 2, '删除员工', 'staff:delete', 4, 1),
(115, 11, 2, '重置密码', 'staff:resetPwd', 5, 1),
(116, 11, 2, '分配角色', 'staff:assignRole', 6, 1);

-- 三级按钮权限：角色管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(121, 12, 2, '查询角色', 'role:query', 1, 1),
(122, 12, 2, '新增角色', 'role:add', 2, 1),
(123, 12, 2, '修改角色', 'role:edit', 3, 1),
(124, 12, 2, '删除角色', 'role:delete', 4, 1),
(125, 12, 2, '分配权限', 'role:assignMenu', 5, 1);

-- 三级按钮权限：组织管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(131, 13, 2, '查询组织', 'org:query', 1, 1),
(132, 13, 2, '新增组织', 'org:add', 2, 1),
(133, 13, 2, '修改组织', 'org:edit', 3, 1),
(134, 13, 2, '删除组织', 'org:delete', 4, 1);

-- 三级按钮权限：菜单管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(211, 21, 2, '查询菜单', 'menu:query', 1, 1),
(212, 21, 2, '新增菜单', 'menu:add', 2, 1),
(213, 21, 2, '修改菜单', 'menu:edit', 3, 1),
(214, 21, 2, '删除菜单', 'menu:delete', 4, 1);

-- 三级按钮权限：字典管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(221, 22, 2, '查询字典', 'dict:query', 1, 1),
(222, 22, 2, '新增字典', 'dict:add', 2, 1),
(223, 22, 2, '修改字典', 'dict:edit', 3, 1),
(224, 22, 2, '删除字典', 'dict:delete', 4, 1);

-- 业务管理（原有的四个管理功能）
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order, visible) VALUES
(3, 0, 0, '业务管理', '/business', NULL, 'ShoppingCart', 3, 1);

INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order, visible) VALUES
(31, 3, 1, '桌台管理', '/business/tables', 'business/Tables', 'table', 1, 1),
(32, 3, 1, '分类管理', '/business/categories', 'business/Categories', 'folder', 2, 1),
(33, 3, 1, '商品管理', '/business/products', 'business/Products', 'box', 3, 1),
(34, 3, 1, '结账管理', '/business/checkout', 'business/Checkout', 'money', 4, 1);

-- 客户管理
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order, visible) VALUES
(4, 0, 0, '客户管理', '/customer', NULL, 'UserGroup', 4, 1);

INSERT INTO t_menu (id, parent_id, menu_type, menu_name, path, component, icon, sort_order, visible) VALUES
(41, 4, 1, '客户列表', '/customer/list', 'customer/CustomerList', 'list', 1, 1);

-- 三级按钮权限：客户列表
INSERT INTO t_menu (id, parent_id, menu_type, menu_name, permission_code, sort_order, visible) VALUES
(411, 41, 2, '查询客户', 'customer:query', 1, 1),
(412, 41, 2, '新增客户', 'customer:add', 2, 1),
(413, 41, 2, '修改客户', 'customer:edit', 3, 1),
(414, 41, 2, '删除客户', 'customer:delete', 4, 1);

-- 初始化角色数据（先清空）
DELETE FROM t_role_menu;
DELETE FROM t_staff_role;
DELETE FROM t_role;

INSERT INTO t_role (id, role_name, role_code, description, data_scope) VALUES
(1, '超级管理员', 'super_admin', '拥有系统所有权限', 1),
(2, '店长', 'store_manager', '管理本店所有业务', 3),
(3, '吧台员工', 'bar_staff', '吧台日常运营', 3),
(4, '后厨员工', 'kitchen_staff', '后厨日常运营', 3);

-- 为超级管理员分配所有菜单权限
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 11), (1, 12), (1, 13), (1, 21), (1, 22), (1, 31), (1, 32), (1, 33), (1, 34), (1, 41),
(1, 111), (1, 112), (1, 113), (1, 114), (1, 115), (1, 116),
(1, 121), (1, 122), (1, 123), (1, 124), (1, 125),
(1, 131), (1, 132), (1, 133), (1, 134),
(1, 211), (1, 212), (1, 213), (1, 214),
(1, 221), (1, 222), (1, 223), (1, 224),
(1, 411), (1, 412), (1, 413), (1, 414);

-- 为店长分配权限
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 11), (2, 12), (2, 13),
(2, 111), (2, 112), (2, 113), (2, 115),
(2, 121), (2, 131);

-- 为吧台员工分配权限
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(3, 1), (3, 11), (3, 111);

-- 为后厨员工分配权限
INSERT INTO t_role_menu (role_id, menu_id) VALUES
(4, 1), (4, 11), (4, 111);

-- 为员工分配角色
INSERT INTO t_staff_role (staff_id, role_id) VALUES (1, 1);
INSERT INTO t_staff_role (staff_id, role_id) VALUES (2, 3);
INSERT INTO t_staff_role (staff_id, role_id) VALUES (3, 4);
INSERT INTO t_staff_role (staff_id, role_id) VALUES (4, 2);

-- ============================================
-- 第二阶段：组织管理
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
DROP PROCEDURE IF EXISTS add_org_id_to_store;
DELIMITER //
CREATE PROCEDURE add_org_id_to_store()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = 'miribar_ordering_single' 
        AND TABLE_NAME = 't_store' 
        AND COLUMN_NAME = 'org_id'
    ) THEN
        ALTER TABLE t_store ADD COLUMN org_id BIGINT COMMENT '所属组织ID';
        ALTER TABLE t_store ADD INDEX idx_org_id (org_id);
    END IF;
END //
DELIMITER ;
CALL add_org_id_to_store();
DROP PROCEDURE IF EXISTS add_org_id_to_store;

-- 初始化组织数据（先清空）
DELETE FROM t_org;

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
-- 第三阶段：字典管理
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

-- 初始化字典类型（先清空）
DELETE FROM t_dict_data;
DELETE FROM t_dict_type;

INSERT INTO t_dict_type (dict_name, dict_type, remark, status) VALUES
('员工初始密码', 'staff_init_password', '员工重置密码时使用的默认密码', 1),
('订单状态', 'order_status', '订单的状态枚举', 1),
('支付方式', 'payment_method', '支付的方式枚举', 1),
('组织类型', 'org_type', '组织的类型枚举', 1),
('菜单类型', 'menu_type', '菜单的类型枚举', 1),
('数据范围', 'data_scope', '角色的数据权限范围', 1);

-- 初始化字典数据
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

SELECT '权限管理系统增量更新完成！' AS message;
