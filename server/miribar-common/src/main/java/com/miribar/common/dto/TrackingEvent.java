package com.miribar.common.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 埋点事件消息体（发送到 Kafka，最终写入 MongoDB）
 */
@Data
public class TrackingEvent {
    /** 事件类型: PAGE_VIEW, PRODUCT_CLICK, ADD_TO_CART, ORDER, PAYMENT */
    private String eventType;
    private Long storeId;
    private Long tableId;
    private Long customerId;
    private Long productId;
    private String page;
    /** 事件来源: MINI_PROGRAM, H5, APP, SYSTEM */
    private String source;
    /** 关联业务ID (商品ID、订单ID等) */
    private Long bizId;
    /** 事件详情 */
    private String detail;
    /** 用户代理 */
    private String userAgent;
    /** 客户端IP */
    private String clientIp;
    /** 事件发生时间 */
    private LocalDateTime eventTime;
    /** 扩展字段 */
    private Map<String, Object> extra;
}
