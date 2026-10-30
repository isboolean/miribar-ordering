-- MiriBar 机构编码改造：英文拼音编码 -> 10 位数字编码（从 000000001 开始）
-- 在已有数据库上执行一次即可；执行后将按 id 顺序重新编号，历史编码不可恢复

USE miribar_ordering_single;

-- 1) 先统一改为临时唯一值，避免重编号过程触发 org_code 唯一索引冲突
UPDATE t_org SET org_code = CONCAT('TMP_', id);

-- 2) 按 id 升序生成序号映射
DROP TEMPORARY TABLE IF EXISTS tmp_org_seq;
CREATE TEMPORARY TABLE tmp_org_seq (
    seq INT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL UNIQUE
);
INSERT INTO tmp_org_seq (org_id) SELECT id FROM t_org ORDER BY id;

-- 3) 写回 10 位数字编码：0000000001、0000000002 ...
UPDATE t_org o
JOIN tmp_org_seq s ON s.org_id = o.id
SET o.org_code = LPAD(s.seq, 10, '0');

DROP TEMPORARY TABLE tmp_org_seq;

-- 4) 收敛字段长度（可选，与 schema.sql 保持一致）
ALTER TABLE t_org MODIFY COLUMN org_code VARCHAR(10) NOT NULL COMMENT '组织编码（10位数字，系统生成）';

-- 校验结果
SELECT id, parent_id, org_name, org_code FROM t_org ORDER BY org_code;
