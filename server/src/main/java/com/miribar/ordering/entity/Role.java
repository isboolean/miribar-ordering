package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Role {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer dataScope; // 1=全部 2=本组织及下级 3=仅本组织
    private Integer status; // 0-禁用 1-启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
