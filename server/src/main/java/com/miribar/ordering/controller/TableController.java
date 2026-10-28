package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.DiningTable;
import com.miribar.ordering.service.TableService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    /**
     * 获取门店桌台列表
     */
    @GetMapping("/store/{storeId}")
    public Result<List<DiningTable>> getTables(@PathVariable Long storeId) {
        return Result.success(tableService.getTables(storeId));
    }

    /**
     * 通过桌码获取桌台信息（扫码入口）
     */
    @GetMapping("/qr/{qrToken}")
    public Result<DiningTable> getTableByQrToken(@PathVariable String qrToken) {
        DiningTable table = tableService.getTableByQrToken(qrToken);
        if (table == null) {
            return Result.error(404, "桌码无效");
        }
        return Result.success(table);
    }

    /**
     * 获取桌台详情
     */
    @GetMapping("/{tableId}")
    public Result<DiningTable> getTableById(@PathVariable Long tableId) {
        return Result.success(tableService.getTableById(tableId));
    }

    /**
     * 新增桌台
     */
    @PostMapping
    public Result<DiningTable> addTable(@RequestBody DiningTable table) {
        return Result.success(tableService.addTable(table));
    }

    /**
     * 更新桌台
     */
    @PutMapping("/{tableId}")
    public Result<Void> updateTable(@PathVariable Long tableId, @RequestBody DiningTable table) {
        table.setId(tableId);
        tableService.updateTable(table);
        return Result.success();
    }

    /**
     * 删除桌台
     */
    @DeleteMapping("/{tableId}")
    public Result<Void> deleteTable(@PathVariable Long tableId) {
        tableService.deleteTable(tableId);
        return Result.success();
    }

    /**
     * 开台
     */
    @PostMapping("/{tableId}/open")
    public Result<Void> openTable(@PathVariable Long tableId) {
        tableService.openTable(tableId);
        return Result.success();
    }

    /**
     * 清台
     */
    @PostMapping("/{tableId}/clear")
    public Result<Void> clearTable(@PathVariable Long tableId) {
        tableService.clearTable(tableId);
        return Result.success();
    }

    /**
     * 获取桌台账单
     */
    @GetMapping("/{tableId}/bill")
    public Result<Map<String, Object>> getTableBill(@PathVariable Long tableId) {
        return Result.success(tableService.getTableBill(tableId));
    }

    /**
     * 结账
     */
    @PostMapping("/{tableId}/checkout")
    public Result<Void> checkout(@PathVariable Long tableId,
                                 @RequestParam(required = false) BigDecimal discountAmount,
                                 @RequestParam(required = false) String remark) {
        tableService.checkout(tableId, discountAmount != null ? discountAmount : BigDecimal.ZERO, remark);
        return Result.success();
    }
}
