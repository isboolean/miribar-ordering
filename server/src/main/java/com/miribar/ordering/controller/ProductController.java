package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.dto.ProductVO;
import com.miribar.ordering.entity.Category;
import com.miribar.ordering.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ========== 分类管理 ==========

    @GetMapping("/category/store/{storeId}")
    public Result<List<Category>> getCategories(@PathVariable Long storeId) {
        return Result.success(productService.getCategories(storeId));
    }

    @PostMapping("/category")
    public Result<Category> addCategory(@RequestBody Category category) {
        return Result.success(productService.addCategory(category));
    }

    @PutMapping("/category/{categoryId}")
    public Result<Void> updateCategory(@PathVariable Long categoryId, @RequestBody Category category) {
        category.setId(categoryId);
        productService.updateCategory(category);
        return Result.success();
    }

    @DeleteMapping("/category/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        productService.deleteCategory(categoryId);
        return Result.success();
    }

    // ========== 商品管理 ==========

    @GetMapping("/store/{storeId}")
    public Result<List<Map<String, Object>>> getProducts(@PathVariable Long storeId) {
        return Result.success(productService.getMenu(storeId));
    }

    @GetMapping("/{productId}")
    public Result<ProductVO> getProductDetail(@PathVariable Long productId) {
        return Result.success(productService.getProductDetail(productId));
    }

    @PostMapping
    public Result<Void> addProduct(@RequestBody ProductVO productVO) {
        productService.addProduct(productVO);
        return Result.success();
    }

    @PutMapping("/{productId}")
    public Result<Void> updateProduct(@PathVariable Long productId, @RequestBody ProductVO productVO) {
        productVO.getProduct().setId(productId);
        productService.updateProduct(productVO);
        return Result.success();
    }

    @PostMapping("/{productId}/status")
    public Result<Void> updateProductStatus(@PathVariable Long productId, @RequestParam Integer status) {
        productService.updateProductStatus(productId, status);
        return Result.success();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return Result.success();
    }
}
