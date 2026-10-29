package com.miribar.common.feign;

import com.miribar.common.entity.DiningTable;
import com.miribar.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 门店服务 Feign Client（order-service 调用）
 */
@FeignClient(name = "miribar-store-service", fallbackFactory = StoreClientFallbackFactory.class)
public interface StoreClient {

    @GetMapping("/api/internal/table/{tableId}")
    Result<DiningTable> getTableById(@PathVariable("tableId") Long tableId);
}
