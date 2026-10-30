package com.miribar.ordering.controller;

import com.miribar.ordering.common.PasswordUtil;
import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.Role;
import com.miribar.ordering.entity.Staff;
import com.miribar.ordering.mapper.StaffMapper;
import com.miribar.ordering.service.DictDataService;
import com.miribar.ordering.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
public class StaffManageController {
    
    private final StaffMapper staffMapper;
    private final RoleService roleService;
    private final DictDataService dictDataService;
    
    // 默认初始密码（从字典管理读取，如果没有则使用此默认值）
    private static final String FALLBACK_PASSWORD = "123456";
    
    public StaffManageController(StaffMapper staffMapper, RoleService roleService, DictDataService dictDataService) {
        this.staffMapper = staffMapper;
        this.roleService = roleService;
        this.dictDataService = dictDataService;
    }
    
    /**
     * 获取初始密码（从字典管理读取）
     */
    private String getInitPassword() {
        String password = dictDataService.getDefaultValue("staff_init_password");
        return password != null ? password : FALLBACK_PASSWORD;
    }
    
    /**
     * 获取员工列表
     */
    @GetMapping("/list")
    public Result<List<Staff>> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String keyword) {
        List<Staff> staffs;
        if (keyword != null && !keyword.trim().isEmpty()) {
            staffs = staffMapper.selectByKeyword(keyword.trim());
        } else if (storeId != null) {
            staffs = staffMapper.selectByStoreId(storeId);
        } else {
            staffs = staffMapper.selectAll();
        }
        // 清除密码字段
        staffs.forEach(s -> s.setPassword(null));
        return Result.success(staffs);
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
        // 检查用户名是否已存在
        Staff existing = staffMapper.selectByUsername(staff.getUsername());
        if (existing != null) {
            return Result.error(400, "用户名已存在");
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
        
        // 如果修改了用户名，检查是否与其他员工冲突
        if (staff.getUsername() != null && !staff.getUsername().equals(existing.getUsername())) {
            Staff conflict = staffMapper.selectByUsername(staff.getUsername());
            if (conflict != null && !conflict.getId().equals(staff.getId())) {
                return Result.error(400, "用户名已存在");
            }
        }
        
        // 不允许通过此接口修改密码
        staff.setPassword(null);
        
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
