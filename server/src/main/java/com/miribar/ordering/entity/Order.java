package com.miribar.ordering.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private Long storeId;
    private Long tableId;
    private Long customerId;
    private String orderNo;
    private Integer status;
    private Integer paymentMethod;
    private Integer paymentStatus;
    private BigDecimal totalAmount;
    private String remark;
    private Integer source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
