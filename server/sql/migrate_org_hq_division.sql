-- ============================================
-- 组织机构层级升级：集团 -> 总部 / 区域分部 -> 省份 -> 城市 -> 店面
--
-- 类型枚举（新）：
--   1=集团  2=总部  3=分部  4=区域  5=总部机构  6=省份  7=城市  8=店面
-- 存量平移：2(省份)->6、3(城市)->7、4(店面)->8
--
-- 幂等：可重复执行（新增节点按 org_code 判重，类型平移只命中旧值 2/3/4）
-- 执行前会备份到 t_org_bak_20261030_3
-- ============================================

USE miribar_ordering_single;

-- 0) 备份（只建一次）
CREATE TABLE IF NOT EXISTS t_org_bak_20261030_3 AS SELECT * FROM t_org;

-- 1) 旧类型平移：省份/城市/店面各自后移 4 位
--    仅作用于存量机构（编码 0000000001~0000000023），避免重复执行时误改新增的总部/分部
UPDATE t_org
SET org_type = CASE org_type
        WHEN 2 THEN 6
        WHEN 3 THEN 7
        WHEN 4 THEN 8
        ELSE org_type
    END
WHERE org_type IN (2, 3, 4)
  AND org_code <= '0000000023';

-- 2a) 总部（type 2），挂在集团下
INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000001' parent_code, '总部' org_name, '0000000024' org_code, 2 org_type,
           '张总' leader, '13800000150' phone, '北京市朝阳区建国路88号 MiriBar 大厦' address, 1 sort_order
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

-- 2b) 总部职能部门（type 5），单独一条 INSERT，确保总部已落库
INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000024' parent_code, '运营中心' org_name, '0000000025' org_code, 5 org_type,
           '周运营' leader, '13800000151' phone, '北京市朝阳区建国路88号 MiriBar 大厦 8F' address, 1 sort_order
    UNION ALL SELECT '0000000024', '财务中心',     '0000000026', 5, '陈财务', '13800000152', '北京市朝阳区建国路88号 MiriBar 大厦 8F', 2
    UNION ALL SELECT '0000000024', '人力资源中心', '0000000027', 5, '吴人事', '13800000153', '北京市朝阳区建国路88号 MiriBar 大厦 9F', 3
    UNION ALL SELECT '0000000024', '技术中心',     '0000000028', 5, '林技术', '13800000154', '北京市朝阳区建国路88号 MiriBar 大厦 9F', 4
    UNION ALL SELECT '0000000024', '供应链中心',   '0000000029', 5, '黄供应', '13800000155', '北京市朝阳区建国路88号 MiriBar 大厦 10F', 5
    UNION ALL SELECT '0000000024', '市场中心',     '0000000030', 5, '孙市场', '13800000156', '北京市朝阳区建国路88号 MiriBar 大厦 10F', 6
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

-- 3) 区域分部（type 3），同样挂在集团下，与总部同级
INSERT INTO t_org (parent_id, org_name, org_code, org_type, leader, phone, address, sort_order, status)
SELECT p.id, v.org_name, v.org_code, v.org_type, v.leader, v.phone, v.address, v.sort_order, 1
FROM (
    SELECT '0000000001' parent_code, '华北分部' org_name, '0000000031' org_code, 3 org_type,
           '刘京生' leader, '13800000157' phone, '北京市' address, 2 sort_order
    UNION ALL SELECT '0000000001', '华东分部', '0000000032', 3, '钱沪生', '13800000158', '上海市', 3
    UNION ALL SELECT '0000000001', '华南分部', '0000000033', 3, '梁粤',   '13800000159', '广东省', 4
) v
JOIN t_org p ON p.org_code = v.parent_code
WHERE NOT EXISTS (SELECT 1 FROM t_org x WHERE x.org_code = v.org_code);

-- 4) 省份改挂到对应分部：北京->华北，上海/浙江->华东，广东->华南
UPDATE t_org p
JOIN t_org d ON d.org_code = CASE p.org_code
        WHEN '0000000002' THEN '0000000031'   -- 北京市
        WHEN '0000000005' THEN '0000000032'   -- 上海市
        WHEN '0000000007' THEN '0000000032'   -- 浙江省
        WHEN '0000000006' THEN '0000000033'   -- 广东省
    END
SET p.parent_id = d.id,
    p.sort_order = CASE p.org_code
        WHEN '0000000002' THEN 1
        WHEN '0000000005' THEN 1
        WHEN '0000000007' THEN 2
        WHEN '0000000006' THEN 1
    END
WHERE p.org_code IN ('0000000002', '0000000005', '0000000006', '0000000007');

-- 5) 原挂在集团下的总部员工，改挂到对应职能部门
UPDATE t_staff s JOIN t_org o ON o.org_code = '0000000025' SET s.org_id = o.id WHERE s.phone = '13800000120';
UPDATE t_staff s JOIN t_org o ON o.org_code = '0000000027' SET s.org_id = o.id WHERE s.phone = '13800000121';

-- 6) 校验：父子关系一览（按 parent_id 排序，便于人工核对层级）
SELECT o.id, o.org_name, o.org_code, o.org_type, p.org_name AS parent_name, o.sort_order
FROM t_org o
LEFT JOIN t_org p ON p.id = o.parent_id
ORDER BY COALESCE(o.parent_id, 0), o.sort_order, o.id;

-- 7) 校验：各层级数量（应为 集团1 / 总部1 / 分部3 / 总部机构6 / 省份4 / 城市10 / 店面7）
SELECT org_type,
       CASE org_type
           WHEN 1 THEN '集团' WHEN 2 THEN '总部' WHEN 3 THEN '分部' WHEN 4 THEN '区域'
           WHEN 5 THEN '总部机构' WHEN 6 THEN '省份' WHEN 7 THEN '城市' WHEN 8 THEN '店面'
           ELSE '未知'
       END AS type_name,
       COUNT(*) AS cnt
FROM t_org
GROUP BY org_type
ORDER BY org_type;

-- 8) 回滚参考（按需手工执行）：
--    UPDATE t_org o JOIN t_org_bak_20261030_3 b ON b.id = o.id
--       SET o.parent_id = b.parent_id, o.org_type = b.org_type, o.sort_order = b.sort_order;
--    DELETE FROM t_org WHERE org_code BETWEEN '0000000024' AND '0000000033';
