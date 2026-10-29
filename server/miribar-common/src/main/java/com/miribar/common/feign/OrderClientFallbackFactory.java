package com.miribar.common.feign;

import com.miribar.common.dto.OrderVO;
import com.miribar.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

/**
 * 订单服务 Feign 降级工厂
 */
public interface OrderClientFallbackFactory extends org.springframework.cloud.openfeign.FallbackFactory<OrderClient> {
}
