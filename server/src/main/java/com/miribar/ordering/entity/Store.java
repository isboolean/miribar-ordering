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
    private Long orgId; // 所属组织ID（t_store.org_id）
    private String orgName; // 所属组织名称（关联查询，非表字段）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
