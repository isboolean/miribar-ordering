package com.miribar.ordering.service;

import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.entity.Role;

import java.util.List;

public interface RoleService {
    
    List<Role> listAll();
    
    List<Role> listEnabled();

    /**
     * 分页查询角色
     *
     * @param roleName  角色名称模糊匹配（可选）
     * @param roleCode  角色编码模糊匹配（可选）
     * @param dataScope 数据范围（可选）
     * @param status    状态（可选）
     * @param page      页码，从 1 开始
     * @param pageSize  每页条数
     */
    PageResult<Role> page(String roleName, String roleCode, Integer dataScope, Integer status,
                          int page, int pageSize);
    
    Role getById(Long id);
    
    Role getByRoleCode(String roleCode);
    
    void add(Role role);
    
    void update(Role role);
    
    void delete(Long id);
    
    void assignMenus(Long roleId, List<Long> menuIds);
    
    List<Long> getAssignedMenuIds(Long roleId);
    
    List<Role> getRolesByStaffId(Long staffId);
    
    void assignRoles(Long staffId, List<Long> roleIds);
}
