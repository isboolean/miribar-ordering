package com.miribar.ordering.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限注解
 * 用于标记需要特定权限的接口方法
 * 使用示例：@RequirePermission("staff:add")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 权限编码
     * 对应 t_menu 表中的 permission_code 字段
     */
    String value();
}
