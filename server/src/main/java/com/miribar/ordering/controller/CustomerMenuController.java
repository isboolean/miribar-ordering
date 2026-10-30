package com.miribar.ordering.controller;

import com.miribar.ordering.common.BusinessException;
import com.miribar.ordering.common.Result;
import com.miribar.ordering.dto.ProductVO;
import com.miribar.ordering.entity.Category;
import com.miribar.ordering.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 顾客端菜单接口（无需登录，见 AuthInterceptor PUBLIC_PATHS）
 * 仅展示上架（status=1）的分类与商品
 */
@RestController
@RequestMapping("/api/menu")
public class CustomerMenuController {

    private final ProductService productService;

    public CustomerMenuController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取门店完整菜单（按分类分组，含SKU，仅上架）
     */
    @GetMapping("/store/{storeId}")
    public Result<List<Map<String, Object>>> getMenu(@PathVariable Long storeId) {
        List<Map<String, Object>> menu = new ArrayList<>();
        for (Map<String, Object> group : productService.getMenu(storeId)) {
            Category category = (Category) group.get("category");
            if (category.getStatus() != null && category.getStatus() != 1) {
                continue;
            }
            @SuppressWarnings("unchecked")
            List<ProductVO> products = (List<ProductVO>) group.get("products");
            List<ProductVO> onSale = products.stream()
                    .filter(p -> p.getProduct().getStatus() == null || p.getProduct().getStatus() == 1)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            if (onSale.isEmpty()) {
                continue;
            }
            Map<String, Object> filtered = new LinkedHashMap<>();
            filtered.put("category", category);
            filtered.put("products", onSale);
            menu.add(filtered);
        }
        return Result.success(menu);
    }

    /**
     * 获取门店分类列表（仅上架）
     */
    @GetMapping("/store/{storeId}/categories")
    public Result<List<Category>> getCategories(@PathVariable Long storeId) {
        List<Category> categories = productService.getCategories(storeId).stream()
                .filter(c -> c.getStatus() == null || c.getStatus() == 1)
                .toList();
        return Result.success(categories);
    }

    /**
     * 获取分类下的商品列表（含SKU，仅上架）
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<ProductVO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductVO> products = productService.getProductsByCategory(categoryId).stream()
                .filter(p -> p.getProduct().getStatus() == null || p.getProduct().getStatus() == 1)
                .toList();
        return Result.success(products);
    }

    /**
     * 获取商品详情（含SKU）
     */
    @GetMapping("/product/{productId}")
    public Result<ProductVO> getProductDetail(@PathVariable Long productId) {
        ProductVO vo = productService.getProductDetail(productId);
        if (vo.getProduct().getStatus() != null && vo.getProduct().getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }
        return Result.success(vo);
    }
}
