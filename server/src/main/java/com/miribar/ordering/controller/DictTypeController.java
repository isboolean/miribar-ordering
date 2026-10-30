package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.DictType;
import com.miribar.ordering.service.DictTypeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dict/type")
public class DictTypeController {
    
    private final DictTypeService dictTypeService;
    
    public DictTypeController(DictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }
    
    /**
     * 获取字典类型列表
     */
    @GetMapping("/list")
    public Result<List<DictType>> list() {
        List<DictType> types = dictTypeService.listAll();
        return Result.success(types);
    }
    
    /**
     * 获取启用的字典类型列表
     */
    @GetMapping("/enabled")
    public Result<List<DictType>> listEnabled() {
        List<DictType> types = dictTypeService.listEnabled();
        return Result.success(types);
    }
    
    /**
     * 获取字典类型详情
     */
    @GetMapping("/{id}")
    public Result<DictType> getById(@PathVariable Long id) {
        DictType type = dictTypeService.getById(id);
        return Result.success(type);
    }
    
    /**
     * 根据字典类型标识获取详情
     */
    @GetMapping("/code/{dictType}")
    public Result<DictType> getByDictType(@PathVariable String dictType) {
        DictType type = dictTypeService.getByDictType(dictType);
        return Result.success(type);
    }
    
    /**
     * 新增字典类型
     */
    @PostMapping
    public Result<Void> add(@RequestBody DictType dictType) {
        dictTypeService.add(dictType);
        return Result.success(null);
    }
    
    /**
     * 修改字典类型
     */
    @PutMapping
    public Result<Void> update(@RequestBody DictType dictType) {
        dictTypeService.update(dictType);
        return Result.success(null);
    }
    
    /**
     * 删除字典类型
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictTypeService.delete(id);
        return Result.success(null);
    }
    
    /**
     * 搜索字典类型
     */
    @GetMapping("/search")
    public Result<List<DictType>> search(@RequestParam String keyword) {
        List<DictType> types = dictTypeService.search(keyword);
        return Result.success(types);
    }
}
