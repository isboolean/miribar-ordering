package com.miribar.product.controller;

import com.miribar.common.dto.ProductVO;
import com.miribar.common.entity.Category;
import com.miribar.common.result.Result;
import com.miribar.product.service.ProductService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    private final ProductService productService;
    public MenuController(ProductService productService) { this.productService = productService; }

    @GetMapping("/store/{storeId}")
    public Result<List<Map<String, Object>>> getMenu(@PathVariable Long storeId) {
        return Result.success(productService.getMenu(storeId));
    }

    @GetMapping("/store/{storeId}/categories")
    public Result<List<Category>> getCategories(@PathVariable Long storeId) {
        return Result.success(productService.getCategories(storeId));
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<ProductVO>> getProductsByCategory(@PathVariable Long categoryId) {
        return Result.success(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/product/{productId}")
    public Result<ProductVO> getProductDetail(@PathVariable Long productId) {
        return Result.success(productService.getProductDetail(productId));
    }
}
