package com.miribar.notification.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

/**
 * MongoDB 埋点事件文档
 * 记录顾客行为：页面浏览、商品点击、加购、下单、支付
 */
@Data
@Document(collection = "tracking_events")
public class TrackingEventDocument {

    @Id
    private String id;

    @Indexed
    private Long storeId;

    @Indexed
    private Long customerId;

    /** 事件类型: PAGE_VIEW, PRODUCT_CLICK, ADD_TO_CART, ORDER, PAYMENT */
    @Indexed
    private String eventType;

    /** 事件来源: MINI_PROGRAM, H5, APP */
    private String source;

    /** 关联业务ID (商品ID、订单ID等) */
    private Long bizId;

    /** 事件详情 (JSON结构) */
    private String detail;

    /** 用户代理 */
    private String userAgent;

    /** 客户端IP */
    private String clientIp;

    /** 事件发生时间 */
    @Indexed
    private LocalDateTime eventTime;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
