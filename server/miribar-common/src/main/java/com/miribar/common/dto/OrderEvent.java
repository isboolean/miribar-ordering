package com.miribar.common.dto;

import lombok.Data;

/**
 * Kafka 订单事件消息体
 */
@Data
public class OrderEvent {
    private String eventType; // CREATED, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    private Long orderId;
    private String orderNo;
    private Long storeId;
    private Long tableId;
    private Integer status;
    private String message;
    private Long timestamp;
}
