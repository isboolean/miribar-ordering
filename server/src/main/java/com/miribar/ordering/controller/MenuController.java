package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.Menu;
import com.miribar.ordering.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/menu")
public class MenuController {
    
    private final MenuService menuService;
    
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    /**
     * 获取菜单列表（树形）
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<Map<String, Object>> tree = menuService.listMenuTree();
        return Result.success(tree);
    }
    
    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    public Result<Menu> getById(@PathVariable Long id) {
        Menu menu = menuService.getById(id);
        return Result.success(menu);
    }
    
    /**
     * 新增菜单
     */
    @PostMapping
    public Result<Void> add(@RequestBody Menu menu) {
        menuService.add(menu);
        return Result.success(null);
    }
    
    /**
     * 修改菜单
     */
    @PutMapping
    public Result<Void> update(@RequestBody Menu menu) {
        menuService.update(menu);
        return Result.success(null);
    }
    
    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success(null);
    }
}
