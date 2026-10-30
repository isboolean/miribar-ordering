-- MiriBar 演示数据：多层级组织机构 + 门店 + 员工
-- 幂等：可重复执行（组织按 org_code 去重，员工按 username 去重，门店按 org_id 去重）
-- 员工初始密码与 admin 相同（沿用 admin 的密码哈希）

USE miribar_ordering_single;

-- ============================================
-- 1. 组织机构
--    层级：集团 -> 总部 / 区域分部 -> 省份 -> 城市 -> 店面
--    分批插入：总部与分部 -> 省 -> 市 -> 店面，保证父节点先存在
--    类型：1=集团 2=总部 3=分部 4=区域 5=总部机构 6=省份 7=城市 8=店面
-- ============================================

-- 1.0 总部（含职能部门）与区域分部
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

-- 1.1 省份（挂在对应分部：上海/浙江 -> 华东分部，广东 -> 华南分部）
INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000032' parent_code, '上海市' org_name, '0000000005' org_code, 6 org_type, '钱沪生' leader, '13800000101' phone, '上海市' address, 1 sort_order
    UNION ALL SELECT '0000000033', '广东省', '0000000006', 6, '梁粤', '13800000102', '广东省', 1
    UNION ALL SELECT '0000000032', '浙江省', '0000000007', 6, '苏杭', '13800000103', '浙江省', 2
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

-- 1.2 城市
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

-- 1.3 店面
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

-- ============================================
-- 2. 门店（每个店面机构一家门店）
-- ============================================
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

-- ============================================
-- 3. 桌台（新门店各 3 张）
-- ============================================
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

-- ============================================
-- 4. 员工（按机构分布，含集团/省/市/店四个层级）
--    账号 = 10 位数字工号；role: 0=吧台 1=后厨 2=店长 3=平台管理员
--    去重依据：手机号（同一员工重复执行不会重复插入）
-- ============================================
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

-- ============================================
-- 5. 角色映射（按 role 字段补齐 t_staff_role）
-- ============================================
INSERT INTO t_staff_role (staff_id, role_id)
SELECT s.id, CASE s.role WHEN 0 THEN 3 WHEN 1 THEN 4 WHEN 2 THEN 2 ELSE 1 END
FROM t_staff s
WHERE NOT EXISTS (SELECT 1 FROM t_staff_role r WHERE r.staff_id = s.id);

-- 校验
SELECT COUNT(*) AS org_count FROM t_org;
SELECT COUNT(*) AS store_count FROM t_store;
SELECT COUNT(*) AS staff_count FROM t_staff;
