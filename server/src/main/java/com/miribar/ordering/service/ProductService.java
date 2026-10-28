package com.miribar.ordering.service;

import com.miribar.ordering.dto.ProductVO;
import com.miribar.ordering.entity.Category;

import java.util.List;
import java.util.Map;

public interface ProductService {

    /**
     * 获取门店菜单（按分类分组，含SKU）
     */
    List<Map<String, Object>> getMenu(Long storeId);

    /**
     * 获取门店分类列表
     */
    List<Category> getCategories(Long storeId);

    /**
     * 获取分类下的商品列表（含SKU）
     */
    List<ProductVO> getProductsByCategory(Long categoryId);

    /**
     * 获取商品详情（含SKU）
     */
    ProductVO getProductDetail(Long productId);

    /**
     * 新增商品
     */
    void addProduct(ProductVO productVO);

    /**
     * 更新商品
     */
    void updateProduct(ProductVO productVO);

    /**
     * 上下架商品
     */
    void updateProductStatus(Long productId, Integer status);

    /**
     * 删除商品
     */
    void deleteProduct(Long productId);

    /**
     * 新增分类
     */
    Category addCategory(Category category);

    /**
     * 更新分类
     */
    void updateCategory(Category category);

    /**
     * 删除分类
     */
    void deleteCategory(Long categoryId);
}
