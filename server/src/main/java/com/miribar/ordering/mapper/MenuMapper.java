package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {
    
    List<Menu> selectAll();
    
    List<Menu> selectByStatus(@Param("status") Integer status);
    
    Menu selectById(@Param("id") Long id);
    
    int insert(Menu menu);
    
    int update(Menu menu);
    
    int deleteById(@Param("id") Long id);
    
    List<Menu> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
    
    List<Menu> selectMenusByRoleId(@Param("roleId") Long roleId);
    
    int deleteMenusByRoleId(@Param("roleId") Long roleId);
}
