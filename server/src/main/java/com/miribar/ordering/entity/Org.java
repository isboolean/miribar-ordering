package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Org {
    private Long id;
    private Long parentId;
    private String orgName;
    private String orgCode;
    private Integer orgType; // 1=集团 2=省份 3=城市 4=店面
    private String leader;
    private String phone;
    private String address;
    private Integer sortOrder;
    private Integer status; // 0-禁用 1-启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
