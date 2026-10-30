package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMenuMapper {
    
    int insert(RoleMenu roleMenu);
    
    int batchInsert(@Param("list") List<RoleMenu> list);
    
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    
    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);
}
