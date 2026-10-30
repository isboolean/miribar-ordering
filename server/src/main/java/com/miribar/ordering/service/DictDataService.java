package com.miribar.ordering.service;

import com.miribar.ordering.entity.DictData;

import java.util.List;

public interface DictDataService {
    
    List<DictData> listAll();
    
    List<DictData> listByDictType(String dictType);
    
    List<DictData> listEnabled();
    
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
