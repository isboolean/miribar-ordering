package com.miribar.ordering.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解
 * 用于标记需要根据用户组织范围过滤数据的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    
    /**
     * 组织表的别名（用于 SQL 拼接）
     */
    String orgAlias() default "org";
    
    /**
     * 组织ID字段名
     */
    String orgIdColumn() default "org_id";
}
