package com.miribar.notification.analytics;

import com.miribar.common.dto.OrderEvent;
import com.miribar.common.dto.TrackingEvent;
import com.miribar.notification.document.TrackingEventDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 埋点事件服务
 * 将顾客行为事件写入 MongoDB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventTrackingService {

    private final MongoTemplate mongoTemplate;

    /**
     * 记录通用埋点事件
     */
    public void trackEvent(TrackingEvent event) {
        TrackingEventDocument doc = new TrackingEventDocument();
        doc.setStoreId(event.getStoreId());
        doc.setCustomerId(event.getCustomerId());
        doc.setEventType(event.getEventType());
        doc.setSource(event.getSource());
        doc.setBizId(event.getBizId());
        doc.setDetail(event.getDetail());
        doc.setUserAgent(event.getUserAgent());
        doc.setClientIp(event.getClientIp());
        doc.setEventTime(event.getEventTime() != null ? event.getEventTime() : LocalDateTime.now());
        doc.setCreatedAt(LocalDateTime.now());

        mongoTemplate.save(doc);
        log.debug("埋点事件已保存: type={}, storeId={}, customerId={}", event.getEventType(), event.getStoreId(), event.getCustomerId());
    }

    /**
     * 根据订单事件生成埋点记录
     */
    public void trackOrderEvent(OrderEvent orderEvent) {
        TrackingEvent event = new TrackingEvent();
        event.setStoreId(orderEvent.getStoreId());
        event.setEventType("ORDER");
        event.setSource("SYSTEM");
        event.setBizId(orderEvent.getOrderId());
        event.setDetail("订单事件: " + orderEvent.getEventType() + ", 订单号: " + orderEvent.getOrderNo());
        event.setEventTime(LocalDateTime.now());

        trackEvent(event);
    }
}
