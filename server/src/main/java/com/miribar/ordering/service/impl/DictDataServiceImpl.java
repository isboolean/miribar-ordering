package com.miribar.ordering.service.impl;

import com.miribar.ordering.common.PageQuery;
import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.entity.DictData;
import com.miribar.ordering.mapper.DictDataMapper;
import com.miribar.ordering.service.DictDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictDataServiceImpl implements DictDataService {
    
    private final DictDataMapper dictDataMapper;
    
    public DictDataServiceImpl(DictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }
    
    @Override
    public List<DictData> listAll() {
        return dictDataMapper.selectAll();
    }
    
    @Override
    public List<DictData> listByDictType(String dictType) {
        return dictDataMapper.selectByDictType(dictType);
    }
    
    @Override
    public List<DictData> listEnabled() {
        return dictDataMapper.selectByStatus(1);
    }

    @Override
    public PageResult<DictData> page(String dictType, String dictLabel, String dictValue, Integer status,
                                     int page, int pageSize) {
        long total = dictDataMapper.countByCondition(dictType, dictLabel, dictValue, status);
        if (total == 0) {
            return PageResult.empty(0, page, pageSize);
        }
        List<DictData> rows = dictDataMapper.selectPage(dictType, dictLabel, dictValue, status,
                PageQuery.offset(page, pageSize), pageSize);
        return PageResult.of(rows, total, page, pageSize);
    }
    
    @Override
    public DictData getById(Long id) {
        return dictDataMapper.selectById(id);
    }
    
    @Override
    public DictData getDefaultByDictType(String dictType) {
        return dictDataMapper.selectDefaultByDictType(dictType);
    }
    
    @Override
    @Transactional
    public void add(DictData dictData) {
        if (dictData.getSortOrder() == null) {
            dictData.setSortOrder(0);
        }
        if (dictData.getStatus() == null) {
            dictData.setStatus(1);
        }
        if (dictData.getIsDefault() == null) {
            dictData.setIsDefault(0);
        }
        
        dictDataMapper.insert(dictData);
    }
    
    @Override
    @Transactional
    public void update(DictData dictData) {
        dictDataMapper.update(dictData);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        dictDataMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteByDictType(String dictType) {
        dictDataMapper.deleteByDictType(dictType);
    }
    
    @Override
    public List<DictData> search(String keyword) {
        return dictDataMapper.selectByKeyword(keyword);
    }
    
    @Override
    public String getDefaultValue(String dictType) {
        DictData defaultData = getDefaultByDictType(dictType);
        return defaultData != null ? defaultData.getDictValue() : null;
    }
    
    @Override
    public String getLabelByValue(String dictType, String dictValue) {
        List<DictData> dataList = listByDictType(dictType);
        for (DictData data : dataList) {
            if (data.getDictValue().equals(dictValue)) {
                return data.getDictLabel();
            }
        }
        return null;
    }
}
