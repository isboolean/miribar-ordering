package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.dto.ProductVO;
import com.miribar.ordering.entity.Category;
import com.miribar.ordering.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final ProductService productService;

    public MenuController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取门店完整菜单（按分类分组）
     */
    @GetMapping("/store/{storeId}")
    public Result<List<Map<String, Object>>> getMenu(@PathVariable Long storeId) {
        return Result.success(productService.getMenu(storeId));
    }

    /**
     * 获取门店分类列表
     */
    @GetMapping("/store/{storeId}/categories")
    public Result<List<Category>> getCategories(@PathVariable Long storeId) {
        return Result.success(productService.getCategories(storeId));
    }

    /**
     * 获取分类下的商品列表
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<ProductVO>> getProductsByCategory(@PathVariable Long categoryId) {
        return Result.success(productService.getProductsByCategory(categoryId));
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/product/{productId}")
    public Result<ProductVO> getProductDetail(@PathVariable Long productId) {
        return Result.success(productService.getProductDetail(productId));
    }
}
