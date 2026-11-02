package com.miribar.ordering.service;

import java.util.List;

/**
 * 店铺数据范围服务
 * 根据当前登录员工的角色数据范围（dataScope）与所属组织，计算其可见/可操作的店铺集合。
 * 无员工登录上下文（顾客端/公开接口）时不做限制。
 */
public interface StoreScopeService {

    /** 当前请求是否携带有效员工登录上下文 */
    boolean hasStaffContext();

    /**
     * 获取当前员工可见的店铺ID集合。
     * 返回 null 表示不做限制（全部数据，或无员工上下文）。
     */
    List<Long> getAllowedStoreIds();

    /**
     * 校验店铺是否在当前员工数据范围内，不在则抛出 BusinessException。
     */
    void checkStoreAllowed(Long storeId);
}
