package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.DictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictDataMapper {
    
    List<DictData> selectAll();
    
    List<DictData> selectByDictType(@Param("dictType") String dictType);
    
    List<DictData> selectByStatus(@Param("status") Integer status);
    
    DictData selectById(@Param("id") Long id);
    
    DictData selectDefaultByDictType(@Param("dictType") String dictType);
    
    int insert(DictData dictData);
    
    int update(DictData dictData);
    
    int deleteById(@Param("id") Long id);
    
    int deleteByDictType(@Param("dictType") String dictType);
    
    List<DictData> selectByKeyword(@Param("keyword") String keyword);
}
