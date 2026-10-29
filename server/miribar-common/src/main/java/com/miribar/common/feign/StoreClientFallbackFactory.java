package com.miribar.common.feign;

import com.miribar.common.entity.DiningTable;
import com.miribar.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 门店服务 Feign 降级工厂
 */
public interface StoreClientFallbackFactory extends org.springframework.cloud.openfeign.FallbackFactory<StoreClient> {
}
