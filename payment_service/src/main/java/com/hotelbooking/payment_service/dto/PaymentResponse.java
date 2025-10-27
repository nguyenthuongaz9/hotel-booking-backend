package com.hotelbooking.payment_service.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    private Long amount;
    private String currency;
    private String orderId;
}