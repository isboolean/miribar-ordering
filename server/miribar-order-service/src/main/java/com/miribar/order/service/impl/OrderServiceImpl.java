package com.miribar.order.service.impl;

import com.miribar.common.dto.OrderEvent;
import com.miribar.common.dto.OrderItemRequest;
import com.miribar.common.dto.OrderRequest;
import com.miribar.common.dto.OrderVO;
import com.miribar.common.entity.*;
import com.miribar.common.exception.BusinessException;
import com.miribar.common.feign.ProductClient;
import com.miribar.common.feign.StoreClient;
import com.miribar.order.kafka.OrderEventProducer;
import com.miribar.order.mapper.OrderItemMapper;
import com.miribar.order.mapper.OrderMapper;
import com.miribar.order.service.OrderService;
import com.miribar.order.websocket.OrderWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductClient productClient;
    private final StoreClient storeClient;
    private final OrderWebSocketHandler webSocketHandler;
    private final OrderEventProducer orderEventProducer;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            ProductClient productClient, StoreClient storeClient,
                            OrderWebSocketHandler webSocketHandler, OrderEventProducer orderEventProducer) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productClient = productClient;
        this.storeClient = storeClient;
        this.webSocketHandler = webSocketHandler;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    @Transactional
    public OrderVO createOrder(OrderRequest request) {
        // 通过 Feign 校验桌台
        var tableResult = storeClient.getTableById(request.getTableId());
        if (tableResult == null || tableResult.getData() == null) throw new BusinessException("桌台不存在");
        DiningTable table = tableResult.getData();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            // 通过 Feign 获取 SKU
            var skuResult = productClient.getSkuById(itemReq.getSkuId());
            if (skuResult == null || skuResult.getData() == null) throw new BusinessException("商品规格不存在: SKU " + itemReq.getSkuId());
            Sku sku = skuResult.getData();
            if (sku.getSoldOut() == 1) throw new BusinessException("商品已售罄: " + sku.getSkuName());
            if (sku.getStock() < itemReq.getQuantity()) throw new BusinessException("库存不足: " + sku.getSkuName());

            var productResult = productClient.getProductDetail(sku.getProductId());
            Product product = productResult != null && productResult.getData() != null ? productResult.getData().getProduct() : null;
            if (product == null || product.getStatus() != 1) throw new BusinessException("商品已下架");

            BigDecimal subtotal = sku.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setSkuId(sku.getId());
            item.setProductName(product.getName());
            item.setSkuName(sku.getSkuName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(sku.getPrice());
            item.setSubtotal(subtotal);
            item.setRemark(itemReq.getRemark());
            item.setItemType(product.getProductType());
            orderItems.add(item);
        }

        String orderNo = generateOrderNo();
        List<Order> existingOrders = orderMapper.selectByTableId(request.getTableId());
        int source = existingOrders.isEmpty() ? 0 : 1;

        Order order = new Order();
        order.setStoreId(request.getStoreId());
        order.setTableId(request.getTableId());
        order.setCustomerId(request.getCustomerId());
        order.setOrderNo(orderNo);
        order.setStatus(0);
        order.setPaymentMethod(0);
        order.setPaymentStatus(0);
        order.setTotalAmount(totalAmount);
        order.setRemark(request.getRemark());
        order.setSource(source);
        orderMapper.insert(order);

        for (OrderItem item : orderItems) item.setOrderId(order.getId());
        orderItemMapper.batchInsert(orderItems);

        OrderVO vo = new OrderVO();
        vo.setOrder(order);
        vo.setItems(orderItems);
        vo.setTableNumber(table.getTableNumber());

        // WebSocket 推送
        webSocketHandler.notifyNewOrder(vo);
        // Kafka 发送订单创建事件
        orderEventProducer.sendOrderEvent("CREATED", order.getId(), order.getOrderNo(), order.getStoreId(), order.getTableId(), 0, "新订单");

        return vo;
    }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        return buildOrderVO(order);
    }

    @Override
    public List<OrderVO> getTableOrders(Long tableId) {
        return orderMapper.selectByTableId(tableId).stream().map(this::buildOrderVO).toList();
    }

    @Override
    public List<OrderVO> getStoreActiveOrders(Long storeId) {
        return orderMapper.selectActiveByStoreId(storeId).stream().map(this::buildOrderVO).toList();
    }

    @Override
    public List<OrderVO> getStoreOrdersByStatus(Long storeId, Integer status) {
        return orderMapper.selectByStoreIdAndStatus(storeId, status).stream().map(this::buildOrderVO).toList();
    }

    @Override
    @Transactional
    public void acceptOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) throw new BusinessException("订单状态不允许接单");
        orderMapper.updateStatus(orderId, 1);
        webSocketHandler.notifyOrderStatusChange(orderId, 1, "已接单");
        orderEventProducer.sendOrderEvent("ACCEPTED", orderId, order.getOrderNo(), order.getStoreId(), order.getTableId(), 1, "已接单");
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) throw new BusinessException("订单状态不允许完成");
        orderMapper.updateStatus(orderId, 2);
        webSocketHandler.notifyOrderStatusChange(orderId, 2, "制作完成");
        orderEventProducer.sendOrderEvent("COMPLETED", orderId, order.getOrderNo(), order.getStoreId(), order.getTableId(), 2, "制作完成");
    }

    @Override
    @Transactional
    public void rejectOrder(Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) throw new BusinessException("订单状态不允许拒单");
        orderMapper.updateStatus(orderId, 3);
        webSocketHandler.notifyOrderStatusChange(orderId, 3, "已拒单: " + reason);
        orderEventProducer.sendOrderEvent("REJECTED", orderId, order.getOrderNo(), order.getStoreId(), order.getTableId(), 3, "已拒单: " + reason);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() > 1) throw new BusinessException("订单状态不允许取消");
        orderMapper.updateStatus(orderId, 4);
        webSocketHandler.notifyOrderStatusChange(orderId, 4, "已取消");
        orderEventProducer.sendOrderEvent("CANCELLED", orderId, order.getOrderNo(), order.getStoreId(), order.getTableId(), 4, "已取消");
    }

    private OrderVO buildOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setOrder(order);
        vo.setItems(orderItemMapper.selectByOrderId(order.getId()));
        try {
            var tableResult = storeClient.getTableById(order.getTableId());
            if (tableResult != null && tableResult.getData() != null) vo.setTableNumber(tableResult.getData().getTableNumber());
        } catch (Exception e) { /* Feign fallback */ }
        return vo;
    }

    private String generateOrderNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "MB" + ts + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
