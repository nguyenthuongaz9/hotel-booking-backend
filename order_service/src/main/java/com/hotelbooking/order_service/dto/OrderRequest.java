package com.hotelbooking.order_service.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {
   
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private String paymentMethod;
    private List<OrderItemRequest> items;
    private List<OrderDiscountRequest> discounts;
    public BigDecimal getTotalAmount() {
        throw new UnsupportedOperationException("Unimplemented method 'getTotalAmount'");
    }
    public BigDecimal getFinalAmount() {
        throw new UnsupportedOperationException("Unimplemented method 'getFinalAmount'");
    }
    public String getPaymentMethod() {
        throw new UnsupportedOperationException("Unimplemented method 'getPaymentMethod'");
    }
    public Object getItems() {
        throw new UnsupportedOperationException("Unimplemented method 'getItems'");
    }
}
