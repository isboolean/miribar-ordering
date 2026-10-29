package com.miribar.common.entity;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
