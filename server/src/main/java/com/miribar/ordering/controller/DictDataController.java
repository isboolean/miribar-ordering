package com.miribar.ordering.controller;

import com.miribar.ordering.common.PageQuery;
import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.DictData;
import com.miribar.ordering.service.DictDataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dict/data")
public class DictDataController {
    
    private final DictDataService dictDataService;
    
    public DictDataController(DictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }
    
    /**
     * 分页获取字典数据列表
     *
     * @param dictType  字典类型（可选，为空表示全部类型）
     * @param dictLabel 标签（模糊，可选）
     * @param dictValue 键值（模糊，可选）
     * @param status    状态（可选）
     * @param page      页码，默认 1
     * @param pageSize  每页条数，默认 10，可选 15 / 30
     */
    @GetMapping("/list")
    public Result<PageResult<DictData>> list(
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) String dictLabel,
            @RequestParam(required = false) String dictValue,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(queryPage(dictType, dictLabel, dictValue, status, page, pageSize));
    }

    /**
     * 根据字典类型分页获取数据列表
     */
    @GetMapping("/type/{dictType}")
    public Result<PageResult<DictData>> listByDictType(
            @PathVariable String dictType,
            @RequestParam(required = false) String dictLabel,
            @RequestParam(required = false) String dictValue,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(queryPage(dictType, dictLabel, dictValue, status, page, pageSize));
    }

    private PageResult<DictData> queryPage(String dictType, String dictLabel, String dictValue,
                                           Integer status, Integer page, Integer pageSize) {
        int current = PageQuery.page(page);
        int size = PageQuery.pageSize(pageSize);
        return dictDataService.page(trimToNull(dictType), trimToNull(dictLabel), trimToNull(dictValue),
                status, current, size);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /**
     * 获取启用的字典数据列表
     */
    @GetMapping("/enabled")
    public Result<List<DictData>> listEnabled() {
        List<DictData> dataList = dictDataService.listEnabled();
        return Result.success(dataList);
    }
    
    /**
     * 获取字典数据详情
     */
    @GetMapping("/{id}")
    public Result<DictData> getById(@PathVariable Long id) {
        DictData data = dictDataService.getById(id);
        return Result.success(data);
    }
    
    /**
     * 获取字典类型的默认值
     */
    @GetMapping("/default/{dictType}")
    public Result<String> getDefaultValue(@PathVariable String dictType) {
        String defaultValue = dictDataService.getDefaultValue(dictType);
        return Result.success(defaultValue);
    }
    
    /**
     * 根据字典类型和值获取标签
     */
    @GetMapping("/label")
    public Result<String> getLabelByValue(@RequestParam String dictType, @RequestParam String dictValue) {
        String label = dictDataService.getLabelByValue(dictType, dictValue);
        return Result.success(label);
    }
    
    /**
     * 新增字典数据
     */
    @PostMapping
    public Result<Void> add(@RequestBody DictData dictData) {
        dictDataService.add(dictData);
        return Result.success(null);
    }
    
    /**
     * 修改字典数据
     */
    @PutMapping
    public Result<Void> update(@RequestBody DictData dictData) {
        dictDataService.update(dictData);
        return Result.success(null);
    }
    
    /**
     * 删除字典数据
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictDataService.delete(id);
        return Result.success(null);
    }
    
    /**
     * 根据字典类型删除所有数据
     */
    @DeleteMapping("/type/{dictType}")
    public Result<Void> deleteByDictType(@PathVariable String dictType) {
        dictDataService.deleteByDictType(dictType);
        return Result.success(null);
    }
    
    /**
     * 搜索字典数据
     */
    @GetMapping("/search")
    public Result<List<DictData>> search(@RequestParam String keyword) {
        List<DictData> dataList = dictDataService.search(keyword);
        return Result.success(dataList);
    }
}
