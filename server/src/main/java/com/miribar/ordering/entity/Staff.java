package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Staff {
    private Long id;
    private Long storeId;
    private Long orgId; // 新增：所属组织ID
    private String username;
    private String password;
    private String phone;
    private String realName;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
