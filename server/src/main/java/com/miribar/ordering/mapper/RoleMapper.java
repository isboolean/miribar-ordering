package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    
    List<Role> selectAll();
    
    List<Role> selectByStatus(@Param("status") Integer status);
    
    Role selectById(@Param("id") Long id);
    
    Role selectByRoleCode(@Param("roleCode") String roleCode);
    
    int insert(Role role);
    
    int update(Role role);
    
    int deleteById(@Param("id") Long id);
    
    List<Role> selectRolesByStaffId(@Param("staffId") Long staffId);
}
