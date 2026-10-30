package com.miribar.ordering.controller;

import com.miribar.ordering.common.PageQuery;
import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.Role;
import com.miribar.ordering.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/role")
public class RoleController {
    
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    /**
     * 分页获取角色列表
     *
     * @param roleName  角色名称（模糊，可选）
     * @param roleCode  角色编码（模糊，可选）
     * @param dataScope 数据范围（可选）
     * @param status    状态（可选）
     * @param page      页码，默认 1
     * @param pageSize  每页条数，默认 10，可选 15 / 30
     */
    @GetMapping("/list")
    public Result<PageResult<Role>> list(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer dataScope,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        int current = PageQuery.page(page);
        int size = PageQuery.pageSize(pageSize);
        PageResult<Role> result = roleService.page(
                trimToNull(roleName), trimToNull(roleCode), dataScope, status, current, size);
        return Result.success(result);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /**
     * 获取启用的角色列表
     */
    @GetMapping("/enabled")
    public Result<List<Role>> listEnabled() {
        List<Role> roles = roleService.listEnabled();
        return Result.success(roles);
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return Result.success(role);
    }
    
    /**
     * 新增角色
     */
    @PostMapping
    public Result<Void> add(@RequestBody Role role) {
        roleService.add(role);
        return Result.success(null);
    }
    
    /**
     * 修改角色
     */
    @PutMapping
    public Result<Void> update(@RequestBody Role role) {
        roleService.update(role);
        return Result.success(null);
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success(null);
    }
    
    /**
     * 分配角色菜单权限
     */
    @PostMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success(null);
    }
    
    /**
     * 获取角色已分配的菜单ID列表
     */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getAssignedMenuIds(@PathVariable Long id) {
        List<Long> menuIds = roleService.getAssignedMenuIds(id);
        return Result.success(menuIds);
    }
}
