package com.miribar.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private Long storeId;
    private Long categoryId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer productType;
    private String tags;
    private Integer containsAlcohol;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
