package com.miribar.order.mapper;

import com.miribar.common.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
    int batchInsert(@Param("list") List<OrderItem> orderItems);
}
