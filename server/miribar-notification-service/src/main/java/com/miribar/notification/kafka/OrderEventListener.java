package com.miribar.notification.kafka;

import com.miribar.common.dto.OrderEvent;
import com.miribar.notification.analytics.EventTrackingService;
import com.miribar.notification.analytics.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 订单事件监听器
 * 消费 order-events topic，处理订单状态变更后的通知、埋点和报表更新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final EventTrackingService eventTrackingService;
    private final ReportService reportService;

    @KafkaListener(topics = "${app.kafka.topics.order-events:order-events}", groupId = "notification-service-order-group")
    public void handleOrderEvent(OrderEvent event) {
        log.info("收到订单事件: type={}, orderId={}, status={}", event.getEventType(), event.getOrderId(), event.getStatus());

        try {
            switch (event.getEventType()) {
                case "order.created" -> {
                    // 记录下单埋点
                    eventTrackingService.trackOrderEvent(event);
                    // 更新报表
                    reportService.onOrderCreated(event);
                }
                case "order.accepted" -> {
                    log.info("订单已接单: orderId={}", event.getOrderId());
                    // 预留: 推送接单通知给顾客
                }
                case "order.rejected" -> {
                    log.info("订单已拒单: orderId={}", event.getOrderId());
                    reportService.onOrderCancelled(event);
                    // 预留: 推送拒单通知给顾客
                }
                case "order.completed" -> {
                    log.info("订单已完成: orderId={}", event.getOrderId());
                    reportService.onOrderCompleted(event);
                    // 预留: 推送完成通知
                }
                case "order.cancelled" -> {
                    log.info("订单已取消: orderId={}", event.getOrderId());
                    reportService.onOrderCancelled(event);
                    // 预留: 推送取消通知
                }
                case "order.status_changed" -> {
                    log.info("订单状态变更: orderId={}, status={}", event.getOrderId(), event.getStatus());
                }
                default -> log.warn("未知订单事件类型: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理订单事件失败: eventType={}, orderId={}", event.getEventType(), event.getOrderId(), e);
        }
    }
}
