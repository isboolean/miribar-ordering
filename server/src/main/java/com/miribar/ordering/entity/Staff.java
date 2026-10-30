package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Staff {
    private Long id;
    private Long storeId;
    private Long orgId; // 所属组织ID
    private String orgName; // 所属组织名称（关联查询，非表字段）
    private String username;
    private String password;
    private String phone;
    private String realName;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
