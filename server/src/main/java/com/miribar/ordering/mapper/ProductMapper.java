package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    List<Product> selectByStoreId(@Param("storeId") Long storeId);

    List<Product> selectByCategoryId(@Param("categoryId") Long categoryId);

    Product selectById(Long id);

    int insert(Product product);

    int update(Product product);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);
}
