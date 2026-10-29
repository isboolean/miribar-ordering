package com.miribar.common.dto;

import com.miribar.common.entity.Order;
import com.miribar.common.entity.OrderItem;
import lombok.Data;
import java.util.List;

@Data
public class OrderVO {
    private Order order;
    private List<OrderItem> items;
    private String tableNumber;
}
