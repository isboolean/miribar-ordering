package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiningTable {
    private Long id;
    private Long storeId;
    private String tableNumber;
    private String qrToken;
    private Integer seats;
    private Integer status;
    private String storeName; // 所属店铺名称（关联查询，非表字段）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
