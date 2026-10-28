package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    int insert(OrderItem orderItem);

    int batchInsert(@Param("list") List<OrderItem> orderItems);
}
