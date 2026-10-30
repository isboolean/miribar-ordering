package com.miribar.ordering.controller;

import com.miribar.ordering.common.BusinessException;
import com.miribar.ordering.common.PasswordUtil;
import com.miribar.ordering.common.PageQuery;
import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.Role;
import com.miribar.ordering.entity.Staff;
import com.miribar.ordering.mapper.StaffMapper;
import com.miribar.ordering.mapper.StoreMapper;
import com.miribar.ordering.service.DictDataService;
import com.miribar.ordering.service.OrgService;
import com.miribar.ordering.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
public class StaffManageController {
    
    private final StaffMapper staffMapper;
    private final StoreMapper storeMapper;
    private final RoleService roleService;
    private final DictDataService dictDataService;
    private final OrgService orgService;
    
    // 默认初始密码（从字典管理读取，如果没有则使用此默认值）
    private static final String FALLBACK_PASSWORD = "123456";

    /** 工号固定 10 位数字，从 0000000001 开始（admin 保留原账号） */
    private static final String STAFF_NO_FORMAT = "%010d";
    private static final long STAFF_NO_MAX = 9999999999L;

    /**
     * 生成下一个工号（10 位数字自增）
     * 加锁避免并发插入拿到相同工号
     */
    private synchronized String nextStaffNo() {
        Long max = staffMapper.selectMaxStaffNo();
        long next = (max == null ? 0L : max) + 1L;
        if (next > STAFF_NO_MAX) {
            throw new BusinessException("工号已用尽（最大 " + STAFF_NO_MAX + "）");
        }
        return String.format(STAFF_NO_FORMAT, next);
    }
    
    public StaffManageController(StaffMapper staffMapper, StoreMapper storeMapper, RoleService roleService,
                                 DictDataService dictDataService, OrgService orgService) {
        this.staffMapper = staffMapper;
        this.storeMapper = storeMapper;
        this.roleService = roleService;
        this.dictDataService = dictDataService;
        this.orgService = orgService;
    }
    
    /**
     * 获取初始密码（从字典管理读取）
     */
    private String getInitPassword() {
        String password = dictDataService.getDefaultValue("staff_init_password");
        return password != null ? password : FALLBACK_PASSWORD;
    }
    
    /**
     * 分页获取员工列表
     *
     * @param storeId  门店ID（可选）
     * @param orgId    机构ID（可选，含该机构下所有下级机构的员工）
     * @param keyword  工号/姓名/手机号（可选）
     * @param phone    手机号（模糊，可选）
     * @param role     角色（可选）
     * @param status   状态（可选）
     * @param page     页码，默认 1
     * @param pageSize 每页条数，默认 10，可选 15 / 30
     */
    @GetMapping("/list")
    public Result<PageResult<Staff>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        int current = PageQuery.page(page);
        int size = PageQuery.pageSize(pageSize);
        List<Long> orgIds = orgId == null ? null : orgService.getOrgTreeIds(orgId);
        String kw = trimToNull(keyword);
        String phoneKw = trimToNull(phone);

        long total = staffMapper.countByCondition(storeId, orgIds, kw, phoneKw, role, status);
        if (total == 0) {
            return Result.success(PageResult.empty(0, current, size));
        }
        List<Staff> staffs = staffMapper.selectPage(storeId, orgIds, kw, phoneKw, role, status,
                PageQuery.offset(current, size), size);
        // 清除密码字段
        staffs.forEach(s -> s.setPassword(null));
        return Result.success(PageResult.of(staffs, total, current, size));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /**
     * 获取员工详情
     */
    @GetMapping("/{id}")
    public Result<Staff> getById(@PathVariable Long id) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) {
            return Result.error(404, "员工不存在");
        }
        staff.setPassword(null);
        return Result.success(staff);
    }
    
    /**
     * 新增员工
     */
    @PostMapping
    public Result<Void> add(@RequestBody Staff staff) {
        // 工号由系统统一生成（10 位数字，从 0000000001 起），忽略前端传入值
        String staffNo = nextStaffNo();
        
        // 检查工号是否已存在（兜底，正常不会命中）
        if (staffMapper.selectByUsername(staffNo) != null) {
            return Result.error(400, "工号生成冲突，请重试");
        }
        staff.setUsername(staffNo);
        
        // 员工必须归属机构，且机构必须存在
        if (staff.getOrgId() == null) {
            return Result.error(400, "请选择所属机构");
        }
        if (orgService.getById(staff.getOrgId()) == null) {
            return Result.error(400, "所属机构不存在");
        }
        
        // 门店ID为空时按机构反查门店，仍取不到则用第一个门店兜底
        if (staff.getStoreId() == null) {
            Long storeId = storeMapper.selectIdByOrgId(staff.getOrgId());
            if (storeId == null) {
                storeId = storeMapper.selectFirstId();
            }
            if (storeId == null) {
                return Result.error(400, "系统还没有门店，请先创建门店");
            }
            staff.setStoreId(storeId);
        }
        
        // 设置默认密码（加密，从字典管理读取）
        staff.setPassword(PasswordUtil.hash(getInitPassword()));
        
        if (staff.getStatus() == null) {
            staff.setStatus(1);
        }
        
        staffMapper.insert(staff);
        return Result.success(null);
    }
    
    /**
     * 修改员工
     */
    @PutMapping
    public Result<Void> update(@RequestBody Staff staff) {
        Staff existing = staffMapper.selectById(staff.getId());
        if (existing == null) {
            return Result.error(404, "员工不存在");
        }
        
        // 工号一经生成不允许修改，密码也不允许通过此接口修改
        staff.setUsername(null);
        staff.setPassword(null);
        
        // 若调整所属机构，校验机构存在
        if (staff.getOrgId() != null && orgService.getById(staff.getOrgId()) == null) {
            return Result.error(400, "所属机构不存在");
        }
        
        staffMapper.update(staff);
        return Result.success(null);
    }
    
    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Staff existing = staffMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "员工不存在");
        }
        
        staffMapper.deleteById(id);
        return Result.success(null);
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        Staff existing = staffMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "员工不存在");
        }
        
        String hashedPassword = PasswordUtil.hash(getInitPassword());
        staffMapper.updatePassword(id, hashedPassword);
        return Result.success(null);
    }
    
    /**
     * 获取员工角色
     */
    @GetMapping("/{id}/roles")
    public Result<List<Role>> getRoles(@PathVariable Long id) {
        List<Role> roles = roleService.getRolesByStaffId(id);
        return Result.success(roles);
    }
    
    /**
     * 分配角色
     */
    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        Staff existing = staffMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "员工不存在");
        }
        
        roleService.assignRoles(id, roleIds);
        return Result.success(null);
    }
}
