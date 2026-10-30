package com.miribar.ordering.service;

import com.miribar.ordering.entity.Menu;

import java.util.List;
import java.util.Map;

public interface MenuService {
    
    List<Menu> listAll();
    
    List<Map<String, Object>> listMenuTree();
    
    Menu getById(Long id);
    
    void add(Menu menu);
    
    void update(Menu menu);
    
    void delete(Long id);
    
    List<Menu> getMenusByRoleIds(List<Long> roleIds);
    
    List<Map<String, Object>> getMenuTreeByRoleIds(List<Long> roleIds);
    
    List<String> getPermissionsByRoleIds(List<Long> roleIds);
}
