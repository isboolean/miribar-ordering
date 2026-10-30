package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.DictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictTypeMapper {
    
    List<DictType> selectAll();
    
    List<DictType> selectByStatus(@Param("status") Integer status);
    
    DictType selectById(@Param("id") Long id);
    
    DictType selectByDictType(@Param("dictType") String dictType);
    
    int insert(DictType dictType);
    
    int update(DictType dictType);
    
    int deleteById(@Param("id") Long id);
    
    List<DictType> selectByKeyword(@Param("keyword") String keyword);
}
