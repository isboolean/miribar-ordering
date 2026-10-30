package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DictData {
    private Long id;
    private String dictType;
    private String dictLabel;
    private String dictValue;
    private String cssClass;
    private String listClass;
    private Integer isDefault; // 0-否 1-是
    private Integer sortOrder;
    private Integer status; // 0-禁用 1-启用
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
