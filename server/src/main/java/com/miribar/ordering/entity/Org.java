package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Org {
    private Long id;
    private Long parentId;
    private String orgName;
    private String orgCode;
    private Integer orgType; // 1=集团 2=总部 3=分部 4=区域 5=总部机构 6=省份 7=城市 8=店面
    private String leader;
    private String phone;
    private String address;
    private Integer sortOrder;
    private Integer status; // 0-禁用 1-启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
