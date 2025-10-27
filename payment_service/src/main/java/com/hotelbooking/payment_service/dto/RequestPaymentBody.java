package com.hotelbooking.payment_service.dto;

import lombok.Data;

@Data
public class RequestPaymentBody {
    private Long amount;
    private String orderId;
    private String currency;
}