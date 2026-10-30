package com.miribar.ordering.controller;

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
     * 获取角色列表
     */
    @GetMapping("/list")
    public Result<List<Role>> list() {
        List<Role> roles = roleService.listAll();
        return Result.success(roles);
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
