package com.miribar.notification.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MongoDB 每日报表文档
 * 按门店/日期聚合的经营数据快照
 */
@Data
@Document(collection = "daily_reports")
@CompoundIndex(name = "idx_store_date", def = "{'storeId': 1, 'reportDate': -1}", unique = true)
public class DailyReport {

    @Id
    private String id;

    @Indexed
    private Long storeId;

    private String storeName;

    @Indexed
    private LocalDate reportDate;

    /** 订单总数 */
    private Integer totalOrders;

    /** 已完成订单数 */
    private Integer completedOrders;

    /** 已取消订单数 */
    private Integer cancelledOrders;

    /** 已拒单数 */
    private Integer rejectedOrders;

    /** 营业额 */
    private BigDecimal totalRevenue;

    /** 平均客单价 */
    private BigDecimal avgOrderAmount;

    /** 新顾客数 */
    private Integer newCustomers;

    /** 活跃顾客数 (下单) */
    private Integer activeCustomers;

    /** 热销商品TOP (JSON数组) */
    private String topProducts;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}
