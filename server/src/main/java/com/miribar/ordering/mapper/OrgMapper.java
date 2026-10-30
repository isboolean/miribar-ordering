package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Org;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrgMapper {
    
    List<Org> selectAll();
    
    List<Org> selectByStatus(@Param("status") Integer status);
    
    Org selectById(@Param("id") Long id);
    
    Org selectByOrgCode(@Param("orgCode") String orgCode);
    
    List<Org> selectByParentId(@Param("parentId") Long parentId);
    
    int insert(Org org);
    
    int update(Org org);
    
    int deleteById(@Param("id") Long id);
    
    List<Org> selectByKeyword(@Param("keyword") String keyword);
}
