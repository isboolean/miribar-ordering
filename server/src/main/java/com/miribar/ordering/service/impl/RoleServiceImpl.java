package com.miribar.ordering.service.impl;

import com.miribar.ordering.common.PageQuery;
import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.entity.Role;
import com.miribar.ordering.entity.RoleMenu;
import com.miribar.ordering.entity.StaffRole;
import com.miribar.ordering.mapper.RoleMapper;
import com.miribar.ordering.mapper.RoleMenuMapper;
import com.miribar.ordering.mapper.StaffRoleMapper;
import com.miribar.ordering.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    
    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final StaffRoleMapper staffRoleMapper;
    
    public RoleServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper, StaffRoleMapper staffRoleMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.staffRoleMapper = staffRoleMapper;
    }
    
    @Override
    public List<Role> listAll() {
        return roleMapper.selectAll();
    }
    
    @Override
    public List<Role> listEnabled() {
        return roleMapper.selectByStatus(1);
    }

    @Override
    public PageResult<Role> page(String roleName, String roleCode, Integer dataScope, Integer status,
                                 int page, int pageSize) {
        long total = roleMapper.countByCondition(roleName, roleCode, dataScope, status);
        if (total == 0) {
            return PageResult.empty(0, page, pageSize);
        }
        List<Role> rows = roleMapper.selectPage(roleName, roleCode, dataScope, status,
                PageQuery.offset(page, pageSize), pageSize);
        return PageResult.of(rows, total, page, pageSize);
    }
    
    @Override
    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }
    
    @Override
    public Role getByRoleCode(String roleCode) {
        return roleMapper.selectByRoleCode(roleCode);
    }
    
    @Override
    @Transactional
    public void add(Role role) {
        // 检查角色编码是否已存在
        Role existing = roleMapper.selectByRoleCode(role.getRoleCode());
        if (existing != null) {
            throw new RuntimeException("角色编码已存在：" + role.getRoleCode());
        }
        
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getDataScope() == null) {
            role.setDataScope(1);
        }
        
        roleMapper.insert(role);
    }
    
    @Override
    @Transactional
    public void update(Role role) {
        // 如果修改了角色编码，检查是否与其他角色冲突
        Role existing = roleMapper.selectById(role.getId());
        if (existing != null && !existing.getRoleCode().equals(role.getRoleCode())) {
            Role conflict = roleMapper.selectByRoleCode(role.getRoleCode());
            if (conflict != null && !conflict.getId().equals(role.getId())) {
                throw new RuntimeException("角色编码已存在：" + role.getRoleCode());
            }
        }
        
        roleMapper.update(role);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        // 检查是否有员工使用此角色
        List<Long> staffIds = staffRoleMapper.selectStaffIdsByRoleId(id);
        if (!staffIds.isEmpty()) {
            throw new RuntimeException("该角色已被分配给员工，无法删除");
        }
        
        // 删除角色菜单关联
        roleMenuMapper.deleteByRoleId(id);
        
        // 删除角色
        roleMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有的关联
        roleMenuMapper.deleteByRoleId(roleId);
        
        // 批量插入新的关联
        if (menuIds != null && !menuIds.isEmpty()) {
            List<RoleMenu> list = new ArrayList<>();
            for (Long menuId : menuIds) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                list.add(rm);
            }
            roleMenuMapper.batchInsert(list);
        }
    }
    
    @Override
    public List<Long> getAssignedMenuIds(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }
    
    @Override
    public List<Role> getRolesByStaffId(Long staffId) {
        return roleMapper.selectRolesByStaffId(staffId);
    }
    
    @Override
    @Transactional
    public void assignRoles(Long staffId, List<Long> roleIds) {
        // 先删除原有的关联
        staffRoleMapper.deleteByStaffId(staffId);
        
        // 批量插入新的关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<StaffRole> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                StaffRole sr = new StaffRole();
                sr.setStaffId(staffId);
                sr.setRoleId(roleId);
                list.add(sr);
            }
            staffRoleMapper.batchInsert(list);
        }
    }
}
