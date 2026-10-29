package com.miribar.order.mapper;

import com.miribar.common.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    Order selectById(Long id);
    List<Order> selectByTableId(@Param("tableId") Long tableId);
    List<Order> selectByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") Integer status);
    List<Order> selectActiveByStoreId(@Param("storeId") Long storeId);
    int insert(Order order);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
