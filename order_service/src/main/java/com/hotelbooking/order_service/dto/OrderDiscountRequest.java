package com.hotelbooking.order_service.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
class OrderDiscountRequest {
    private String discountId;
    private BigDecimal appliedValue;
}