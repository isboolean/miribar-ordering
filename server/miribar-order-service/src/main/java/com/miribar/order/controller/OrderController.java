package com.miribar.order.controller;

import com.miribar.common.dto.OrderRequest;
import com.miribar.common.dto.OrderVO;
import com.miribar.common.result.Result;
import com.miribar.order.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public Result<OrderVO> createOrder(@RequestBody OrderRequest request,
                                       @RequestHeader(value = "X-Customer-Id", required = false) String customerIdHeader) {
        if (customerIdHeader != null && !customerIdHeader.isEmpty() && !"null".equals(customerIdHeader)) {
            request.setCustomerId(Long.parseLong(customerIdHeader));
        }
        return Result.success(orderService.createOrder(request));
    }

    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    @GetMapping("/table/{tableId}")
    public Result<List<OrderVO>> getTableOrders(@PathVariable Long tableId) {
        return Result.success(orderService.getTableOrders(tableId));
    }

    @GetMapping("/store/{storeId}/active")
    public Result<List<OrderVO>> getStoreActiveOrders(@PathVariable Long storeId) {
        return Result.success(orderService.getStoreActiveOrders(storeId));
    }

    @GetMapping("/store/{storeId}/status/{status}")
    public Result<List<OrderVO>> getStoreOrdersByStatus(@PathVariable Long storeId, @PathVariable Integer status) {
        return Result.success(orderService.getStoreOrdersByStatus(storeId, status));
    }

    @PostMapping("/{orderId}/accept")
    public Result<Void> acceptOrder(@PathVariable Long orderId) {
        orderService.acceptOrder(orderId);
        return Result.success();
    }

    @PostMapping("/{orderId}/complete")
    public Result<Void> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return Result.success();
    }

    @PostMapping("/{orderId}/reject")
    public Result<Void> rejectOrder(@PathVariable Long orderId, @RequestParam String reason) {
        orderService.rejectOrder(orderId, reason);
        return Result.success();
    }

    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return Result.success();
    }

    // ===== 内部 Feign 接口 =====
    @GetMapping("/internal/order/table/{tableId}")
    public Result<List<OrderVO>> getTableOrdersInternal(@PathVariable Long tableId) {
        return Result.success(orderService.getTableOrders(tableId));
    }
}
