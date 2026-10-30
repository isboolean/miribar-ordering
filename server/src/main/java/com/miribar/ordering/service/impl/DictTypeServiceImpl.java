package com.miribar.ordering.service.impl;

import com.miribar.ordering.common.PageQuery;
import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.entity.DictType;
import com.miribar.ordering.mapper.DictTypeMapper;
import com.miribar.ordering.service.DictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictTypeServiceImpl implements DictTypeService {
    
    private final DictTypeMapper dictTypeMapper;
    
    public DictTypeServiceImpl(DictTypeMapper dictTypeMapper) {
        this.dictTypeMapper = dictTypeMapper;
    }
    
    @Override
    public List<DictType> listAll() {
        return dictTypeMapper.selectAll();
    }
    
    @Override
    public List<DictType> listEnabled() {
        return dictTypeMapper.selectByStatus(1);
    }

    @Override
    public PageResult<DictType> page(String dictName, String dictType, Integer status,
                                     int page, int pageSize) {
        long total = dictTypeMapper.countByCondition(dictName, dictType, status);
        if (total == 0) {
            return PageResult.empty(0, page, pageSize);
        }
        List<DictType> rows = dictTypeMapper.selectPage(dictName, dictType, status,
                PageQuery.offset(page, pageSize), pageSize);
        return PageResult.of(rows, total, page, pageSize);
    }
    
    @Override
    public DictType getById(Long id) {
        return dictTypeMapper.selectById(id);
    }
    
    @Override
    public DictType getByDictType(String dictType) {
        return dictTypeMapper.selectByDictType(dictType);
    }
    
    @Override
    @Transactional
    public void add(DictType dictType) {
        // 检查字典类型是否已存在
        DictType existing = dictTypeMapper.selectByDictType(dictType.getDictType());
        if (existing != null) {
            throw new RuntimeException("字典类型已存在：" + dictType.getDictType());
        }
        
        if (dictType.getStatus() == null) {
            dictType.setStatus(1);
        }
        
        dictTypeMapper.insert(dictType);
    }
    
    @Override
    @Transactional
    public void update(DictType dictType) {
        dictTypeMapper.update(dictType);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        dictTypeMapper.deleteById(id);
    }
    
    @Override
    public List<DictType> search(String keyword) {
        return dictTypeMapper.selectByKeyword(keyword);
    }
}
