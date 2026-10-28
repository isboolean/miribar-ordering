package com.miribar.ordering.service.impl;

import com.miribar.ordering.common.BusinessException;
import com.miribar.ordering.entity.DiningTable;
import com.miribar.ordering.entity.Order;
import com.miribar.ordering.entity.OrderItem;
import com.miribar.ordering.mapper.OrderItemMapper;
import com.miribar.ordering.mapper.OrderMapper;
import com.miribar.ordering.mapper.TableMapper;
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

    public TableServiceImpl(TableMapper tableMapper, OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.tableMapper = tableMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public List<DiningTable> getTables(Long storeId) {
        return tableMapper.selectByStoreId(storeId);
    }

    @Override
    public DiningTable getTableByQrToken(String qrToken) {
        return tableMapper.selectByQrToken(qrToken);
    }

    @Override
    public DiningTable getTableById(Long tableId) {
        return tableMapper.selectById(tableId);
    }

    @Override
    public DiningTable addTable(DiningTable table) {
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
        tableMapper.update(table);
    }

    @Override
    public void deleteTable(Long tableId) {
        DiningTable table = tableMapper.selectById(tableId);
        if (table != null && table.getStatus() == 1) {
            throw new BusinessException("桌台已开台，无法删除");
        }
        tableMapper.deleteById(tableId);
    }

    @Override
    public void openTable(Long tableId) {
        DiningTable table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException("桌台不存在");
        }
        tableMapper.updateStatus(tableId, 1);
    }

    @Override
    public void clearTable(Long tableId) {
        DiningTable table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException("桌台不存在");
        }
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
        DiningTable table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException("桌台不存在");
        }

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
        bill.put("totalAmount", totalAmount);
        bill.put("orders", orderDetails);
        bill.put("orderCount", orderDetails.size());
        return bill;
    }

    @Override
    @Transactional
    public void checkout(Long tableId, BigDecimal discountAmount, String remark) {
        DiningTable table = tableMapper.selectById(tableId);
        if (table == null) {
            throw new BusinessException("桌台不存在");
        }

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
}
