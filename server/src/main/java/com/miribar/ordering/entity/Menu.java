package com.miribar.ordering.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Menu {
    private Long id;
    private Long parentId;
    private Integer menuType; // 0=目录 1=菜单 2=按钮/权限
    private String menuName;
    private String path;
    private String component;
    private String permissionCode;
    private String icon;
    private Integer sortOrder;
    private Integer visible; // 0-隐藏 1-显示
    private Integer status; // 0-禁用 1-启用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
