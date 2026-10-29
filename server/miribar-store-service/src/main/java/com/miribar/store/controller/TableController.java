package com.miribar.store.controller;

import com.miribar.common.entity.DiningTable;
import com.miribar.common.result.Result;
import com.miribar.store.service.TableService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/table")
public class TableController {
    private final TableService tableService;
    public TableController(TableService tableService) { this.tableService = tableService; }

    @GetMapping("/store/{storeId}")
    public Result<List<DiningTable>> getTables(@PathVariable Long storeId) {
        return Result.success(tableService.getTables(storeId));
    }

    @GetMapping("/qr/{qrToken}")
    public Result<DiningTable> getTableByQrToken(@PathVariable String qrToken) {
        DiningTable table = tableService.getTableByQrToken(qrToken);
        if (table == null) return Result.error(404, "桌码无效");
        return Result.success(table);
    }

    @GetMapping("/{tableId}")
    public Result<DiningTable> getTableById(@PathVariable Long tableId) {
        return Result.success(tableService.getTableById(tableId));
    }

    @PostMapping
    public Result<DiningTable> addTable(@RequestBody DiningTable table) {
        return Result.success(tableService.addTable(table));
    }

    @PutMapping("/{tableId}")
    public Result<Void> updateTable(@PathVariable Long tableId, @RequestBody DiningTable table) {
        table.setId(tableId);
        tableService.updateTable(table);
        return Result.success();
    }

    @DeleteMapping("/{tableId}")
    public Result<Void> deleteTable(@PathVariable Long tableId) {
        tableService.deleteTable(tableId);
        return Result.success();
    }

    @PostMapping("/{tableId}/open")
    public Result<Void> openTable(@PathVariable Long tableId) {
        tableService.openTable(tableId);
        return Result.success();
    }

    @PostMapping("/{tableId}/clear")
    public Result<Void> clearTable(@PathVariable Long tableId) {
        tableService.clearTable(tableId);
        return Result.success();
    }

    @GetMapping("/{tableId}/bill")
    public Result<Map<String, Object>> getTableBill(@PathVariable Long tableId) {
        return Result.success(tableService.getTableBill(tableId));
    }

    @PostMapping("/{tableId}/checkout")
    public Result<Void> checkout(@PathVariable Long tableId,
                                 @RequestParam(required = false) BigDecimal discountAmount,
                                 @RequestParam(required = false) String remark) {
        tableService.checkout(tableId, discountAmount != null ? discountAmount : BigDecimal.ZERO, remark);
        return Result.success();
    }

    // ===== 内部 Feign 接口 =====
    @GetMapping("/internal/table/{tableId}")
    public Result<DiningTable> getTableByIdInternal(@PathVariable Long tableId) {
        return Result.success(tableService.getTableById(tableId));
    }
}
