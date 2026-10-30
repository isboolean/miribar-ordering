package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.Org;
import com.miribar.ordering.service.OrgService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/org")
public class OrgController {
    
    private final OrgService orgService;
    
    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }
    
    /**
     * 获取组织列表（树形）
     */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree() {
        List<Map<String, Object>> tree = orgService.listOrgTree();
        return Result.success(tree);
    }
    
    /**
     * 获取组织列表（平铺）
     */
    @GetMapping("/list")
    public Result<List<Org>> list() {
        List<Org> orgs = orgService.listAll();
        return Result.success(orgs);
    }
    
    /**
     * 获取启用的组织列表
     */
    @GetMapping("/enabled")
    public Result<List<Org>> listEnabled() {
        List<Org> orgs = orgService.listEnabled();
        return Result.success(orgs);
    }
    
    /**
     * 获取组织详情
     */
    @GetMapping("/{id}")
    public Result<Org> getById(@PathVariable Long id) {
        Org org = orgService.getById(id);
        return Result.success(org);
    }
    
    /**
     * 新增组织
     */
    @PostMapping
    public Result<Void> add(@RequestBody Org org) {
        orgService.add(org);
        return Result.success(null);
    }
    
    /**
     * 修改组织
     */
    @PutMapping
    public Result<Void> update(@RequestBody Org org) {
        orgService.update(org);
        return Result.success(null);
    }
    
    /**
     * 删除组织
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orgService.delete(id);
        return Result.success(null);
    }
    
    /**
     * 获取子组织列表
     */
    @GetMapping("/{id}/children")
    public Result<List<Org>> getChildren(@PathVariable Long id) {
        List<Org> children = orgService.getChildren(id);
        return Result.success(children);
    }
    
    /**
     * 搜索组织
     */
    @GetMapping("/search")
    public Result<List<Org>> search(@RequestParam String keyword) {
        List<Org> orgs = orgService.search(keyword);
        return Result.success(orgs);
    }
}
