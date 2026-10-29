package com.miribar.product.service;

import com.miribar.common.dto.ProductVO;
import com.miribar.common.entity.Category;
import com.miribar.common.entity.Sku;
import java.util.List;
import java.util.Map;

public interface ProductService {
    List<Map<String, Object>> getMenu(Long storeId);
    List<Category> getCategories(Long storeId);
    List<ProductVO> getProductsByCategory(Long categoryId);
    ProductVO getProductDetail(Long productId);
    void addProduct(ProductVO productVO);
    void updateProduct(ProductVO productVO);
    void updateProductStatus(Long productId, Integer status);
    void deleteProduct(Long productId);
    Category addCategory(Category category);
    void updateCategory(Category category);
    void deleteCategory(Long categoryId);
    Sku getSkuById(Long skuId);
}
