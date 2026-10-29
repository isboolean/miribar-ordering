package com.miribar.order.websocket;

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

@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderWebSocketHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket disconnected: {}", session.getId());
    }

    public void notifyNewOrder(Object orderVO) { broadcast("NEW_ORDER", orderVO); }

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
        try {
            String json = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(json);
            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    synchronized (session) {
                        try { session.sendMessage(textMessage); } catch (IOException e) { log.error("发送WebSocket消息失败", e); }
                    }
                }
            });
        } catch (Exception e) { log.error("序列化WebSocket消息失败", e); }
    }
}
