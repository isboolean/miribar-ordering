package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.dto.OrderRequest;
import com.miribar.ordering.dto.OrderVO;
import com.miribar.ordering.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单（顾客下单）
     */
    @PostMapping
    public Result<OrderVO> createOrder(@RequestBody OrderRequest request) {
        return Result.success(orderService.createOrder(request));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    /**
     * 获取桌台的所有订单
     */
    @GetMapping("/table/{tableId}")
    public Result<List<OrderVO>> getTableOrders(@PathVariable Long tableId) {
        return Result.success(orderService.getTableOrders(tableId));
    }

    /**
     * 获取门店活跃订单（看板用）
     */
    @GetMapping("/store/{storeId}/active")
    public Result<List<OrderVO>> getStoreActiveOrders(@PathVariable Long storeId) {
        return Result.success(orderService.getStoreActiveOrders(storeId));
    }

    /**
     * 获取门店指定状态订单
     */
    @GetMapping("/store/{storeId}/status/{status}")
    public Result<List<OrderVO>> getStoreOrdersByStatus(
            @PathVariable Long storeId, @PathVariable Integer status) {
        return Result.success(orderService.getStoreOrdersByStatus(storeId, status));
    }

    /**
     * 接单
     */
    @PostMapping("/{orderId}/accept")
    public Result<Void> acceptOrder(@PathVariable Long orderId) {
        orderService.acceptOrder(orderId);
        return Result.success();
    }

    /**
     * 完成制作
     */
    @PostMapping("/{orderId}/complete")
    public Result<Void> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return Result.success();
    }

    /**
     * 拒单
     */
    @PostMapping("/{orderId}/reject")
    public Result<Void> rejectOrder(@PathVariable Long orderId, @RequestParam String reason) {
        orderService.rejectOrder(orderId, reason);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return Result.success();
    }
}
