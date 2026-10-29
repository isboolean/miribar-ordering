package com.miribar.order.service;

import com.miribar.common.dto.OrderRequest;
import com.miribar.common.dto.OrderVO;
import java.util.List;

public interface OrderService {
    OrderVO createOrder(OrderRequest request);
    OrderVO getOrderDetail(Long orderId);
    List<OrderVO> getTableOrders(Long tableId);
    List<OrderVO> getStoreActiveOrders(Long storeId);
    List<OrderVO> getStoreOrdersByStatus(Long storeId, Integer status);
    void acceptOrder(Long orderId);
    void completeOrder(Long orderId);
    void rejectOrder(Long orderId, String reason);
    void cancelOrder(Long orderId);
}
