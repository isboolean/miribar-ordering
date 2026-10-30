package com.miribar.ordering.service;

import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.entity.DictType;

import java.util.List;

public interface DictTypeService {
    
    List<DictType> listAll();
    
    List<DictType> listEnabled();

    /**
     * 分页查询字典类型
     *
     * @param dictName 字典名称模糊匹配（可选）
     * @param dictType 字典编码模糊匹配（可选）
     * @param status   状态（可选）
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     */
    PageResult<DictType> page(String dictName, String dictType, Integer status, int page, int pageSize);
    
    DictType getById(Long id);
    
    DictType getByDictType(String dictType);
    
    void add(DictType dictType);
    
    void update(DictType dictType);
    
    void delete(Long id);
    
    List<DictType> search(String keyword);
}
