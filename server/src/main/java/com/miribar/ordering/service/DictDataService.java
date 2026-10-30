package com.miribar.ordering.service;

import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.entity.DictData;

import java.util.List;

public interface DictDataService {
    
    List<DictData> listAll();
    
    List<DictData> listByDictType(String dictType);
    
    List<DictData> listEnabled();

    /**
     * 分页查询字典数据
     *
     * @param dictType  字典类型（可选，为空表示全部类型）
     * @param dictLabel 标签模糊匹配（可选）
     * @param dictValue 键值模糊匹配（可选）
     * @param status    状态（可选）
     * @param page      页码，从 1 开始
     * @param pageSize  每页条数
     */
    PageResult<DictData> page(String dictType, String dictLabel, String dictValue, Integer status,
                              int page, int pageSize);
    
    DictData getById(Long id);
    
    DictData getDefaultByDictType(String dictType);
    
    void add(DictData dictData);
    
    void update(DictData dictData);
    
    void delete(Long id);
    
    void deleteByDictType(String dictType);
    
    List<DictData> search(String keyword);
    
    /**
     * 根据字典类型获取默认值
     */
    String getDefaultValue(String dictType);
    
    /**
     * 根据字典类型和字典值获取标签
     */
    String getLabelByValue(String dictType, String dictValue);
}
