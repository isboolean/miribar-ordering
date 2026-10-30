package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DictType {
    private Long id;
    private String dictName;
    private String dictType;
    private String remark;
    private Integer status; // 0-禁用 1-启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
