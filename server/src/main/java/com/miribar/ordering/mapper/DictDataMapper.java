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

    /**
     * 分页查询字典数据
     *
     * @param dictType  字典类型（可选，为空表示全部类型）
     * @param dictLabel 标签模糊匹配（可选）
     * @param dictValue 键值模糊匹配（可选）
     * @param status    状态（可选）
     * @param offset    偏移量
     * @param pageSize  每页条数
     */
    List<DictData> selectPage(@Param("dictType") String dictType,
                              @Param("dictLabel") String dictLabel,
                              @Param("dictValue") String dictValue,
                              @Param("status") Integer status,
                              @Param("offset") int offset,
                              @Param("pageSize") int pageSize);

    /** 统计满足条件的字典数据总数（与 selectPage 条件一致） */
    long countByCondition(@Param("dictType") String dictType,
                          @Param("dictLabel") String dictLabel,
                          @Param("dictValue") String dictValue,
                          @Param("status") Integer status);
}
