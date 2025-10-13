package com.hotelbooking.order_service.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
class OrderItemRequest {
    private String zoneId;
    private String ticketTypeId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
