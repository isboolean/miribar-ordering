package com.miribar.ordering.controller;

import com.miribar.ordering.common.PageQuery;
import com.miribar.ordering.common.PageResult;
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
     * 分页获取字典类型列表
     *
     * @param dictName 字典名称（模糊，可选）
     * @param dictType 字典编码（模糊，可选）
     * @param status   状态（可选）
     * @param page     页码，默认 1
     * @param pageSize 每页条数，默认 10，可选 15 / 30
     */
    @GetMapping("/list")
    public Result<PageResult<DictType>> list(
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        int current = PageQuery.page(page);
        int size = PageQuery.pageSize(pageSize);
        PageResult<DictType> result = dictTypeService.page(
                trimToNull(dictName), trimToNull(dictType), status, current, size);
        return Result.success(result);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
