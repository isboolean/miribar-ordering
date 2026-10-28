package com.miribar.ordering.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long storeId;
    private Long tableId;
    private Long customerId;
    private String remark;
    private List<OrderItemRequest> items;
}
