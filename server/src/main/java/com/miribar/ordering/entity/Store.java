package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Store {
    private Long id;
    private String name;
    private String address;
    private String businessHours;
    private Integer paymentMode;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
