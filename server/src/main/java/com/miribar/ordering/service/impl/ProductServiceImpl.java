package com.miribar.ordering.service.impl;

import com.miribar.ordering.common.BusinessException;
import com.miribar.ordering.dto.ProductVO;
import com.miribar.ordering.entity.Category;
import com.miribar.ordering.entity.Product;
import com.miribar.ordering.entity.Sku;
import com.miribar.ordering.mapper.CategoryMapper;
import com.miribar.ordering.mapper.ProductMapper;
import com.miribar.ordering.mapper.SkuMapper;
import com.miribar.ordering.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final SkuMapper skuMapper;

    public ProductServiceImpl(ProductMapper productMapper, CategoryMapper categoryMapper, SkuMapper skuMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.skuMapper = skuMapper;
    }

    @Override
    public List<Map<String, Object>> getMenu(Long storeId) {
        List<Category> categories = categoryMapper.selectByStoreId(storeId);
        List<Map<String, Object>> menu = new ArrayList<>();

        for (Category category : categories) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("category", category);

            List<Product> products = productMapper.selectByCategoryId(category.getId());
            List<ProductVO> productVOs = products.stream().map(p -> {
                ProductVO vo = new ProductVO();
                vo.setProduct(p);
                vo.setSkus(skuMapper.selectByProductId(p.getId()));
                return vo;
            }).collect(Collectors.toList());

            group.put("products", productVOs);
            menu.add(group);
        }
        return menu;
    }

    @Override
    public List<Category> getCategories(Long storeId) {
        return categoryMapper.selectByStoreId(storeId);
    }

    @Override
    public List<ProductVO> getProductsByCategory(Long categoryId) {
        List<Product> products = productMapper.selectByCategoryId(categoryId);
        return products.stream().map(p -> {
            ProductVO vo = new ProductVO();
            vo.setProduct(p);
            vo.setSkus(skuMapper.selectByProductId(p.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ProductVO getProductDetail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        ProductVO vo = new ProductVO();
        vo.setProduct(product);
        vo.setSkus(skuMapper.selectByProductId(productId));
        return vo;
    }

    @Override
    @Transactional
    public void addProduct(ProductVO productVO) {
        Product product = productVO.getProduct();
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        productMapper.insert(product);

        if (productVO.getSkus() != null) {
            for (Sku sku : productVO.getSkus()) {
                sku.setProductId(product.getId());
                skuMapper.insert(sku);
            }
        }
    }

    @Override
    @Transactional
    public void updateProduct(ProductVO productVO) {
        Product product = productVO.getProduct();
        productMapper.update(product);

        if (productVO.getSkus() != null) {
            for (Sku sku : productVO.getSkus()) {
                if (sku.getId() != null) {
                    skuMapper.update(sku);
                } else {
                    sku.setProductId(product.getId());
                    skuMapper.insert(sku);
                }
            }
        }
    }

    @Override
    public void updateProductStatus(Long productId, Integer status) {
        productMapper.updateStatus(productId, status);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        List<Sku> skus = skuMapper.selectByProductId(productId);
        for (Sku sku : skus) {
            skuMapper.deleteById(sku.getId());
        }
        productMapper.deleteById(productId);
    }

    @Override
    public Category addCategory(Category category) {
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public void updateCategory(Category category) {
        categoryMapper.update(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        categoryMapper.deleteById(categoryId);
    }
}
