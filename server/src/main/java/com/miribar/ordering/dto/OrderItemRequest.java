package com.miribar.ordering.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long skuId;
    private Integer quantity;
    private String remark;
}
