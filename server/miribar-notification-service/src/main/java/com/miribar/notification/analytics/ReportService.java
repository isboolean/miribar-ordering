package com.miribar.notification.analytics;

import com.miribar.common.dto.OrderEvent;
import com.miribar.notification.document.DailyReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报表聚合服务
 * 基于订单事件实时更新每日经营报表 (MongoDB)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final MongoTemplate mongoTemplate;

    /**
     * 订单创建时更新报表
     */
    public void onOrderCreated(OrderEvent event) {
        updateDailyReport(event.getStoreId(), update -> {
            update.inc("totalOrders", 1);
        });
    }

    /**
     * 订单完成时更新报表
     */
    public void onOrderCompleted(OrderEvent event) {
        updateDailyReport(event.getStoreId(), update -> {
            update.inc("completedOrders", 1);
            // 金额信息可从 message 中解析，此处简化
        });
    }

    /**
     * 订单取消/拒绝时更新报表
     */
    public void onOrderCancelled(OrderEvent event) {
        updateDailyReport(event.getStoreId(), update -> {
            String eventType = event.getEventType();
            if ("order.rejected".equals(eventType)) {
                update.inc("rejectedOrders", 1);
            } else {
                update.inc("cancelledOrders", 1);
            }
        });
    }

    /**
     * Upsert 每日报表
     */
    private void updateDailyReport(Long storeId, java.util.function.Consumer<Update> updateConsumer) {
        LocalDate today = LocalDate.now();

        Query query = new Query(Criteria.where("storeId").is(storeId)
                .and("reportDate").is(today));

        Update update = new Update();
        updateConsumer.accept(update);
        update.set("updatedAt", LocalDateTime.now());
        update.setOnInsert("storeId", storeId);
        update.setOnInsert("reportDate", today);
        update.setOnInsert("totalOrders", 0);
        update.setOnInsert("completedOrders", 0);
        update.setOnInsert("cancelledOrders", 0);
        update.setOnInsert("rejectedOrders", 0);
        update.setOnInsert("totalRevenue", BigDecimal.ZERO);
        update.setOnInsert("avgOrderAmount", BigDecimal.ZERO);
        update.setOnInsert("newCustomers", 0);
        update.setOnInsert("activeCustomers", 0);
        update.setOnInsert("createdAt", LocalDateTime.now());

        mongoTemplate.upsert(query, update, DailyReport.class);
        log.debug("报表已更新: storeId={}, date={}", storeId, today);
    }

    /**
     * 查询门店某日报表
     */
    public DailyReport getDailyReport(Long storeId, LocalDate date) {
        Query query = new Query(Criteria.where("storeId").is(storeId)
                .and("reportDate").is(date));
        return mongoTemplate.findOne(query, DailyReport.class);
    }

    /**
     * 查询门店日期范围报表
     */
    public java.util.List<DailyReport> getDailyReports(Long storeId, LocalDate startDate, LocalDate endDate) {
        Query query = new Query(Criteria.where("storeId").is(storeId)
                .and("reportDate").gte(startDate).lte(endDate));
        return mongoTemplate.find(query, DailyReport.class);
    }
}
