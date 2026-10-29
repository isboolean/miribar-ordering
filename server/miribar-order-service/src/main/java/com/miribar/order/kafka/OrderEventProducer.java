package com.miribar.order.kafka;

import com.miribar.common.dto.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "order-events";
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderEvent(String eventType, Long orderId, String orderNo, Long storeId, Long tableId, Integer status, String message) {
        OrderEvent event = new OrderEvent();
        event.setEventType(eventType);
        event.setOrderId(orderId);
        event.setOrderNo(orderNo);
        event.setStoreId(storeId);
        event.setTableId(tableId);
        event.setStatus(status);
        event.setMessage(message);
        event.setTimestamp(System.currentTimeMillis());
        kafkaTemplate.send(TOPIC, eventType, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Kafka 订单事件发送失败: {} orderId={}", eventType, orderId, ex);
            } else {
                log.info("Kafka 订单事件发送成功: {} orderId={}", eventType, orderId);
            }
        });
    }
}
