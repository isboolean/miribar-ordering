package com.miribar.ordering.service;

import com.miribar.ordering.entity.DiningTable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TableService {

    /**
     * 获取门店桌台列表
     */
    List<DiningTable> getTables(Long storeId);

    /**
     * 通过桌码获取桌台
     */
    DiningTable getTableByQrToken(String qrToken);

    /**
     * 获取桌台详情
     */
    DiningTable getTableById(Long tableId);

    /**
     * 新增桌台
     */
    DiningTable addTable(DiningTable table);

    /**
     * 更新桌台
     */
    void updateTable(DiningTable table);

    /**
     * 删除桌台
     */
    void deleteTable(Long tableId);

    /**
     * 开台
     */
    void openTable(Long tableId);

    /**
     * 清台
     */
    void clearTable(Long tableId);

    /**
     * 获取桌台账单（合并本桌所有订单）
     */
    Map<String, Object> getTableBill(Long tableId);

    /**
     * 结账
     */
    void checkout(Long tableId, BigDecimal discountAmount, String remark);
}
