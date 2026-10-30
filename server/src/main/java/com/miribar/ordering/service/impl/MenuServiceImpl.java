package com.miribar.ordering.service.impl;

import com.miribar.ordering.entity.Menu;
import com.miribar.ordering.mapper.MenuMapper;
import com.miribar.ordering.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {
    
    private final MenuMapper menuMapper;
    
    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }
    
    @Override
    public List<Menu> listAll() {
        return menuMapper.selectAll();
    }
    
    @Override
    public List<Map<String, Object>> listMenuTree() {
        List<Menu> allMenus = menuMapper.selectAll();
        return buildMenuTree(allMenus, 0L);
    }
    
    @Override
    public Menu getById(Long id) {
        return menuMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public void add(Menu menu) {
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        menuMapper.insert(menu);
    }
    
    @Override
    @Transactional
    public void update(Menu menu) {
        menuMapper.update(menu);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        // 检查是否有子菜单
        List<Menu> allMenus = menuMapper.selectAll();
        boolean hasChildren = allMenus.stream()
                .anyMatch(m -> id.equals(m.getParentId()));
        if (hasChildren) {
            throw new RuntimeException("存在子菜单，无法删除");
        }
        menuMapper.deleteById(id);
    }
    
    @Override
    public List<Menu> getMenusByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return menuMapper.selectByRoleIds(roleIds);
    }
    
    @Override
    public List<Map<String, Object>> getMenuTreeByRoleIds(List<Long> roleIds) {
        List<Menu> menus = getMenusByRoleIds(roleIds);
        return buildMenuTree(menus, 0L);
    }
    
    @Override
    public List<String> getPermissionsByRoleIds(List<Long> roleIds) {
        List<Menu> menus = getMenusByRoleIds(roleIds);
        return menus.stream()
                .filter(m -> m.getMenuType() == 2 && m.getPermissionCode() != null)
                .map(Menu::getPermissionCode)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * 构建菜单树
     */
    private List<Map<String, Object>> buildMenuTree(List<Menu> menus, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        
        for (Menu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", menu.getId());
                node.put("parentId", menu.getParentId());
                node.put("menuType", menu.getMenuType());
                node.put("menuName", menu.getMenuName());
                node.put("path", menu.getPath());
                node.put("component", menu.getComponent());
                node.put("permissionCode", menu.getPermissionCode());
                node.put("icon", menu.getIcon());
                node.put("sortOrder", menu.getSortOrder());
                node.put("visible", menu.getVisible());
                node.put("status", menu.getStatus());
                
                List<Map<String, Object>> children = buildMenuTree(menus, menu.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                
                tree.add(node);
            }
        }
        
        // 按排序字段排序
        tree.sort((a, b) -> {
            Integer sortA = (Integer) a.get("sortOrder");
            Integer sortB = (Integer) b.get("sortOrder");
            if (sortA == null) sortA = 0;
            if (sortB == null) sortB = 0;
            return sortA.compareTo(sortB);
        });
        
        return tree;
    }
}
