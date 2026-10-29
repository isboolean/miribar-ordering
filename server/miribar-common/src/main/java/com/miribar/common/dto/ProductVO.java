package com.miribar.common.dto;

import com.miribar.common.entity.Product;
import com.miribar.common.entity.Sku;
import lombok.Data;
import java.util.List;

@Data
public class ProductVO {
    private Product product;
    private List<Sku> skus;
}
