package com.miribar.ordering.service.impl;

import com.miribar.ordering.common.BusinessException;
import com.miribar.ordering.entity.DiningTable;
import com.miribar.ordering.entity.Order;
import com.miribar.ordering.entity.Store;
import com.miribar.ordering.mapper.OrderItemMapper;
import com.miribar.ordering.mapper.OrderMapper;
import com.miribar.ordering.mapper.StoreMapper;
import com.miribar.ordering.mapper.TableMapper;
import com.miribar.ordering.service.StoreScopeService;
import com.miribar.ordering.service.TableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class TableServiceImpl implements TableService {

    private final TableMapper tableMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StoreMapper storeMapper;
    private final StoreScopeService storeScopeService;

    public TableServiceImpl(TableMapper tableMapper, OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            StoreMapper storeMapper, StoreScopeService storeScopeService) {
        this.tableMapper = tableMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.storeMapper = storeMapper;
        this.storeScopeService = storeScopeService;
    }

    @Override
    public List<DiningTable> getTables(Long storeId) {
        storeScopeService.checkStoreAllowed(storeId);
        return tableMapper.selectByStoreId(storeId);
    }

    @Override
    public DiningTable getTableByQrToken(String qrToken) {
        DiningTable table = tableMapper.selectByQrToken(qrToken);
        if (table != null) {
            // 顾客扫码（无员工上下文）直接放行，员工访问时按店铺校验
            storeScopeService.checkStoreAllowed(table.getStoreId());
        }
        return table;
    }

    @Override
    public DiningTable getTableById(Long tableId) {
        DiningTable table = tableMapper.selectById(tableId);
        if (table != null) {
            storeScopeService.checkStoreAllowed(table.getStoreId());
        }
        return table;
    }

    @Override
    public DiningTable addTable(DiningTable table) {
        if (table.getStoreId() == null) {
            throw new BusinessException("请选择所属店铺");
        }
        Store store = storeMapper.selectById(table.getStoreId());
        if (store == null) {
            throw new BusinessException("所属店铺不存在");
        }
        storeScopeService.checkStoreAllowed(table.getStoreId());
        if (table.getStatus() == null) {
            table.setStatus(0);
        }
        // 生成桌码token
        if (table.getQrToken() == null) {
            table.setQrToken(UUID.randomUUID().toString().replace("-", ""));
        }
        tableMapper.insert(table);
        return table;
    }

    @Override
    public void updateTable(DiningTable table) {
        DiningTable existing = requireTable(table.getId());
        storeScopeService.checkStoreAllowed(existing.getStoreId());
        // 桌台归属店铺不允许通过编辑变更
        table.setStoreId(existing.getStoreId());
        tableMapper.update(table);
    }

    @Override
    public void deleteTable(Long tableId) {
        DiningTable table = requireTable(tableId);
        storeScopeService.checkStoreAllowed(table.getStoreId());
        if (table.getStatus() == 1) {
            throw new BusinessException("桌台已开台，无法删除");
        }
        tableMapper.deleteById(tableId);
    }

    @Override
    public void openTable(Long tableId) {
        DiningTable table = requireTable(tableId);
        storeScopeService.checkStoreAllowed(table.getStoreId());
        tableMapper.updateStatus(tableId, 1);
    }

    @Override
    public void clearTable(Long tableId) {
        DiningTable table = requireTable(tableId);
        storeScopeService.checkStoreAllowed(table.getStoreId());
        // 检查是否有未结账订单
        List<Order> orders = orderMapper.selectByTableId(tableId);
        boolean hasUnsettled = orders.stream()
                .anyMatch(o -> o.getStatus() < 5 && o.getStatus() != 3 && o.getStatus() != 4);
        if (hasUnsettled) {
            throw new BusinessException("存在未结账订单，无法清台");
        }
        tableMapper.updateStatus(tableId, 0);
    }

    @Override
    public Map<String, Object> getTableBill(Long tableId) {
        DiningTable table = requireTable(tableId);
        storeScopeService.checkStoreAllowed(table.getStoreId());

        List<Order> orders = orderMapper.selectByTableId(tableId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Map<String, Object>> orderDetails = new ArrayList<>();

        for (Order order : orders) {
            // 只统计有效订单（非取消、非拒单）
            if (order.getStatus() == 3 || order.getStatus() == 4) {
                continue;
            }
            totalAmount = totalAmount.add(order.getTotalAmount());

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("order", order);
            detail.put("items", orderItemMapper.selectByOrderId(order.getId()));
            orderDetails.add(detail);
        }

        Map<String, Object> bill = new LinkedHashMap<>();
        bill.put("table", table);
        bill.put("storeId", table.getStoreId());
        bill.put("storeName", table.getStoreName());
        bill.put("totalAmount", totalAmount);
        bill.put("orders", orderDetails);
        bill.put("orderCount", orderDetails.size());
        return bill;
    }

    @Override
    @Transactional
    public void checkout(Long tableId, BigDecimal discountAmount, String remark) {
        DiningTable table = requireTable(tableId);
        storeScopeService.checkStoreAllowed(table.getStoreId());

        List<Order> orders = orderMapper.selectByTableId(tableId);
        boolean hasActiveOrders = orders.stream()
                .anyMatch(o -> o.getStatus() == 0 || o.getStatus() == 1);
        if (hasActiveOrders) {
            throw new BusinessException("存在进行中的订单，无法结账");
        }

        // 将所有已完成订单标记为已结账
        for (Order order : orders) {
            if (order.getStatus() == 2) {
                orderMapper.updateStatus(order.getId(), 5);
            }
        }

        // 清台
        tableMapper.updateStatus(tableId, 0);
    }

    private DiningTable requireTable(Long tableId) {
        DiningTable table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException("桌台不存在");
        }
        return table;
    }
}
