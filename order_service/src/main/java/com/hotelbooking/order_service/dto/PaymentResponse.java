package com.hotelbooking.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private PaymentStatus paymentStatus;
    private String message;
}