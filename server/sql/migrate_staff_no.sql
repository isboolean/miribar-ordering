-- MiriBar 员工账号改造：拼音账号 -> 10 位数字工号（admin 保留原账号）
-- 在已有数据库上执行一次；按 id 升序重新编号，原账号不可恢复（备份表可回滚）

USE miribar_ordering_single;

-- 1) 备份
CREATE TABLE IF NOT EXISTS t_staff_bak_account AS SELECT * FROM t_staff;

-- 2) 按 id 升序生成序号映射（排除 admin）
DROP TEMPORARY TABLE IF EXISTS tmp_staff_no;
CREATE TEMPORARY TABLE tmp_staff_no (
    seq INT AUTO_INCREMENT PRIMARY KEY,
    staff_id BIGINT NOT NULL UNIQUE
);
INSERT INTO tmp_staff_no (staff_id)
SELECT id FROM t_staff WHERE username <> 'admin' ORDER BY id;

-- 3) 写回 10 位工号：0000000001、0000000002 ...
UPDATE t_staff s
JOIN tmp_staff_no t ON t.staff_id = s.id
SET s.username = LPAD(t.seq, 10, '0')
WHERE s.username <> 'admin';

DROP TEMPORARY TABLE tmp_staff_no;

-- 校验
SELECT id, username AS 工号, real_name AS 姓名, phone AS 手机号 FROM t_staff ORDER BY id;

-- 回滚（如需）：
-- UPDATE t_staff s JOIN t_staff_bak_account b ON b.id = s.id SET s.username = b.username;
