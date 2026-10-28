package com.miribar.ordering.service.impl;

import com.miribar.ordering.common.BusinessException;
import com.miribar.ordering.dto.OrderItemRequest;
import com.miribar.ordering.dto.OrderRequest;
import com.miribar.ordering.dto.OrderVO;
import com.miribar.ordering.entity.DiningTable;
import com.miribar.ordering.entity.Order;
import com.miribar.ordering.entity.OrderItem;
import com.miribar.ordering.entity.Product;
import com.miribar.ordering.entity.Sku;
import com.miribar.ordering.mapper.*;
import com.miribar.ordering.service.OrderService;
import com.miribar.ordering.websocket.OrderWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final TableMapper tableMapper;
    private final SkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final OrderWebSocketHandler webSocketHandler;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            TableMapper tableMapper, SkuMapper skuMapper,
                            ProductMapper productMapper, OrderWebSocketHandler webSocketHandler) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.tableMapper = tableMapper;
        this.skuMapper = skuMapper;
        this.productMapper = productMapper;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    @Transactional
    public OrderVO createOrder(OrderRequest request) {
        // 校验桌台
        DiningTable table = tableMapper.selectById(request.getTableId());
        if (table == null) {
            throw new BusinessException("桌台不存在");
        }

        // 校验商品并计算金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            Sku sku = skuMapper.selectById(itemReq.getSkuId());
            if (sku == null) {
                throw new BusinessException("商品规格不存在: SKU " + itemReq.getSkuId());
            }
            if (sku.getSoldOut() == 1) {
                Product product = productMapper.selectById(sku.getProductId());
                throw new BusinessException("商品已售罄: " + (product != null ? product.getName() : "") + " " + sku.getSkuName());
            }
            if (sku.getStock() < itemReq.getQuantity()) {
                Product product = productMapper.selectById(sku.getProductId());
                throw new BusinessException("库存不足: " + (product != null ? product.getName() : "") + " " + sku.getSkuName());
            }

            Product product = productMapper.selectById(sku.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException("商品已下架");
            }

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

            // 预占库存
            skuMapper.updateStock(sku.getId(), sku.getStock() - itemReq.getQuantity());
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 判断是否加单
        List<Order> existingOrders = orderMapper.selectByTableId(request.getTableId());
        int source = existingOrders.isEmpty() ? 0 : 1;

        // 创建订单
        Order order = new Order();
        order.setStoreId(request.getStoreId());
        order.setTableId(request.getTableId());
        order.setCustomerId(request.getCustomerId());
        order.setOrderNo(orderNo);
        order.setStatus(0); // 待接单
        order.setPaymentMethod(0); // 线下
        order.setPaymentStatus(0); // 未支付
        order.setTotalAmount(totalAmount);
        order.setRemark(request.getRemark());
        order.setSource(source);
        orderMapper.insert(order);

        // 创建订单项
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemMapper.batchInsert(orderItems);

        // 开台
        if (table.getStatus() == 0) {
            tableMapper.updateStatus(table.getId(), 1);
        }

        // 组装返回
        OrderVO vo = new OrderVO();
        vo.setOrder(order);
        vo.setItems(orderItems);
        vo.setTableNumber(table.getTableNumber());

        // WebSocket 推送新订单到看板
        webSocketHandler.notifyNewOrder(vo);

        return vo;
    }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return buildOrderVO(order);
    }

    @Override
    public List<OrderVO> getTableOrders(Long tableId) {
        List<Order> orders = orderMapper.selectByTableId(tableId);
        List<OrderVO> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(buildOrderVO(order));
        }
        return result;
    }

    @Override
    public List<OrderVO> getStoreActiveOrders(Long storeId) {
        List<Order> orders = orderMapper.selectActiveByStoreId(storeId);
        List<OrderVO> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(buildOrderVO(order));
        }
        return result;
    }

    @Override
    public List<OrderVO> getStoreOrdersByStatus(Long storeId, Integer status) {
        List<Order> orders = orderMapper.selectByStoreIdAndStatus(storeId, status);
        List<OrderVO> result = new ArrayList<>();
        for (Order order : orders) {
            result.add(buildOrderVO(order));
        }
        return result;
    }

    @Override
    @Transactional
    public void acceptOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许接单");
        }
        orderMapper.updateStatus(orderId, 1); // 制作中
        webSocketHandler.notifyOrderStatusChange(orderId, 1, "已接单");
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不允许完成");
        }
        orderMapper.updateStatus(orderId, 2); // 已完成
        webSocketHandler.notifyOrderStatusChange(orderId, 2, "制作完成");
    }

    @Override
    @Transactional
    public void rejectOrder(Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许拒单");
        }
        orderMapper.updateStatus(orderId, 3); // 已拒单

        // 释放库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                skuMapper.updateStock(sku.getId(), sku.getStock() + item.getQuantity());
            }
        }

        webSocketHandler.notifyOrderStatusChange(orderId, 3, "已拒单: " + reason);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() > 1) {
            throw new BusinessException("订单状态不允许取消");
        }
        orderMapper.updateStatus(orderId, 4); // 已取消

        // 释放库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                skuMapper.updateStock(sku.getId(), sku.getStock() + item.getQuantity());
            }
        }

        webSocketHandler.notifyOrderStatusChange(orderId, 4, "已取消");
    }

    private OrderVO buildOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setOrder(order);
        vo.setItems(orderItemMapper.selectByOrderId(order.getId()));

        DiningTable table = tableMapper.selectById(order.getTableId());
        if (table != null) {
            vo.setTableNumber(table.getTableNumber());
        }
        return vo;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "MB" + timestamp + random;
    }
}
