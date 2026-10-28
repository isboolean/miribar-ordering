package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> selectByStoreId(@Param("storeId") Long storeId);

    Category selectById(Long id);

    int insert(Category category);

    int update(Category category);

    int deleteById(Long id);
}
