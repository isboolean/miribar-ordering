package com.miribar.store.service;

import com.miribar.common.entity.DiningTable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TableService {
    List<DiningTable> getTables(Long storeId);
    DiningTable getTableByQrToken(String qrToken);
    DiningTable getTableById(Long tableId);
    DiningTable addTable(DiningTable table);
    void updateTable(DiningTable table);
    void deleteTable(Long tableId);
    void openTable(Long tableId);
    void clearTable(Long tableId);
    Map<String, Object> getTableBill(Long tableId);
    void checkout(Long tableId, BigDecimal discountAmount, String remark);
}
