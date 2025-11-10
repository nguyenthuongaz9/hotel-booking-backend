package com.hotelbooking.order_service.dto;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod; 
    private String successUrl;
    private String cancelUrl;
    private String customerEmail;
}