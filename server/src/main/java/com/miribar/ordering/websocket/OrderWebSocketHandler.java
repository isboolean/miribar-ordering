package com.miribar.ordering.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单看板 WebSocket 处理器
 * 用于实时推送新订单和订单状态变更到吧台/后厨看板
 */
@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderWebSocketHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 门店 -> 会话列表（吧台看板）
    private final Map<String, WebSocketSession> barSessions = new ConcurrentHashMap<>();
    // 门店 -> 会话列表（后厨看板）
    private final Map<String, WebSocketSession> kitchenSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String path = session.getUri().getPath();
        // /ws/kitchen/{storeId} 或 /ws/bar/{storeId}
        String[] parts = path.split("/");
        if (parts.length >= 4) {
            String storeId = parts[3];
            if (path.contains("/kitchen/")) {
                kitchenSessions.put(storeId + "_" + session.getId(), session);
            } else {
                barSessions.put(storeId + "_" + session.getId(), session);
            }
        }
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        barSessions.entrySet().removeIf(e -> e.getValue().getId().equals(session.getId()));
        kitchenSessions.entrySet().removeIf(e -> e.getValue().getId().equals(session.getId()));
        log.info("WebSocket disconnected: {}", session.getId());
    }

    /**
     * 推送新订单通知
     */
    public void notifyNewOrder(Object orderVO) {
        broadcast("NEW_ORDER", orderVO);
    }

    /**
     * 推送订单状态变更
     */
    public void notifyOrderStatusChange(Long orderId, Integer status, String message) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("orderId", orderId);
        data.put("status", status);
        data.put("message", message);
        broadcast("ORDER_STATUS_CHANGE", data);
    }

    private void broadcast(String type, Object data) {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", type);
        message.put("data", data);

        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("序列化WebSocket消息失败", e);
            return;
        }

        TextMessage textMessage = new TextMessage(json);

        // 推送到所有吧台会话
        barSessions.values().forEach(session -> sendMessage(session, textMessage));
        // 推送到所有后厨会话
        kitchenSessions.values().forEach(session -> sendMessage(session, textMessage));
    }

    private void sendMessage(WebSocketSession session, TextMessage message) {
        if (session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                log.error("发送WebSocket消息失败: {}", session.getId(), e);
            }
        }
    }
}
