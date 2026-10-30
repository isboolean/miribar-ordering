package com.miribar.ordering.service;

import com.miribar.ordering.entity.Role;

import java.util.List;

public interface RoleService {
    
    List<Role> listAll();
    
    List<Role> listEnabled();
    
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
