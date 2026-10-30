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

    /**
     * 分页查询字典类型
     *
     * @param dictName 字典名称模糊匹配（可选）
     * @param dictType 字典编码模糊匹配（可选）
     * @param status   状态（可选）
     * @param offset   偏移量
     * @param pageSize 每页条数
     */
    List<DictType> selectPage(@Param("dictName") String dictName,
                              @Param("dictType") String dictType,
                              @Param("status") Integer status,
                              @Param("offset") int offset,
                              @Param("pageSize") int pageSize);

    /** 统计满足条件的字典类型总数（与 selectPage 条件一致） */
    long countByCondition(@Param("dictName") String dictName,
                          @Param("dictType") String dictType,
                          @Param("status") Integer status);
}
