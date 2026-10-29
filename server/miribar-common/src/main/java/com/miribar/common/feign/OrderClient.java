package com.miribar.common.feign;

import com.miribar.common.dto.OrderVO;
import com.miribar.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

/**
 * 订单服务 Feign Client（store-service 结账时调用）
 */
@FeignClient(name = "miribar-order-service", fallbackFactory = OrderClientFallbackFactory.class)
public interface OrderClient {

    @GetMapping("/api/internal/order/table/{tableId}")
    Result<List<OrderVO>> getTableOrders(@PathVariable("tableId") Long tableId);
}
