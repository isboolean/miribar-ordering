package com.miribar.ordering.dto;

import com.miribar.ordering.entity.Order;
import com.miribar.ordering.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderVO {
    private Order order;
    private List<OrderItem> items;
    private String tableNumber;
}
