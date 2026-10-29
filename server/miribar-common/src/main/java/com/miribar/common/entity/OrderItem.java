package com.miribar.common.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long skuId;
    private String productName;
    private String skuName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String remark;
    private Integer itemType;
    private LocalDateTime createdAt;
}
