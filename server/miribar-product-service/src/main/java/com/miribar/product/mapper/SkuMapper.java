package com.miribar.product.mapper;

import com.miribar.common.entity.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SkuMapper {
    List<Sku> selectByProductId(@Param("productId") Long productId);
    Sku selectById(Long id);
    int insert(Sku sku);
    int update(Sku sku);
    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);
    int updateSoldOut(@Param("id") Long id, @Param("soldOut") Integer soldOut);
    int deleteById(Long id);
}
