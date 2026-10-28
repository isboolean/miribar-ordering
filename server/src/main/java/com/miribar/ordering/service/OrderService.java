package com.miribar.ordering.service;

import com.miribar.ordering.dto.OrderRequest;
import com.miribar.ordering.dto.OrderVO;

import java.util.List;

public interface OrderService {

    /**
     * 创建订单
     */
    OrderVO createOrder(OrderRequest request);

    /**
     * 获取订单详情
     */
    OrderVO getOrderDetail(Long orderId);

    /**
     * 获取桌台的所有活跃订单
     */
    List<OrderVO> getTableOrders(Long tableId);

    /**
     * 获取门店活跃订单（看板用）
     */
    List<OrderVO> getStoreActiveOrders(Long storeId);

    /**
     * 获取门店指定状态订单
     */
    List<OrderVO> getStoreOrdersByStatus(Long storeId, Integer status);

    /**
     * 接单
     */
    void acceptOrder(Long orderId);

    /**
     * 完成制作
     */
    void completeOrder(Long orderId);

    /**
     * 拒单
     */
    void rejectOrder(Long orderId, String reason);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId);
}
