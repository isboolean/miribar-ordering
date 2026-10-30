package com.miribar.ordering.service;

import com.miribar.ordering.entity.DictType;

import java.util.List;

public interface DictTypeService {
    
    List<DictType> listAll();
    
    List<DictType> listEnabled();
    
    DictType getById(Long id);
    
    DictType getByDictType(String dictType);
    
    void add(DictType dictType);
    
    void update(DictType dictType);
    
    void delete(Long id);
    
    List<DictType> search(String keyword);
}
