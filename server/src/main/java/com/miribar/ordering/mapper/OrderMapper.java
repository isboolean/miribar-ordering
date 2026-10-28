package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    Order selectById(Long id);

    Order selectByOrderNo(@Param("orderNo") String orderNo);

    List<Order> selectByTableId(@Param("tableId") Long tableId);

    List<Order> selectByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") Integer status);

    List<Order> selectActiveByStoreId(@Param("storeId") Long storeId);

    int insert(Order order);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updatePaymentStatus(@Param("id") Long id, @Param("paymentStatus") Integer paymentStatus);
}
