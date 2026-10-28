package com.miribar.ordering.dto;

import com.miribar.ordering.entity.Product;
import com.miribar.ordering.entity.Sku;
import lombok.Data;

import java.util.List;

@Data
public class ProductVO {
    private Product product;
    private List<Sku> skus;
}
