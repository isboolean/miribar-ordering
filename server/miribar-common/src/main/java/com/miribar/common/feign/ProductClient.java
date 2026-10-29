package com.miribar.common.feign;

import com.miribar.common.dto.ProductVO;
import com.miribar.common.entity.Sku;
import com.miribar.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务 Feign Client（order-service 调用）
 */
@FeignClient(name = "miribar-product-service", fallbackFactory = ProductClientFallbackFactory.class)
public interface ProductClient {

    @GetMapping("/api/internal/product/{productId}")
    Result<ProductVO> getProductDetail(@PathVariable("productId") Long productId);

    @GetMapping("/api/internal/sku/{skuId}")
    Result<Sku> getSkuById(@PathVariable("skuId") Long skuId);
}
