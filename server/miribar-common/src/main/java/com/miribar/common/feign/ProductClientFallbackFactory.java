package com.miribar.common.feign;

import com.miribar.common.dto.ProductVO;
import com.miribar.common.entity.Sku;
import com.miribar.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务 Feign 降级工厂
 */
public interface ProductClientFallbackFactory extends org.springframework.cloud.openfeign.FallbackFactory<ProductClient> {
}
