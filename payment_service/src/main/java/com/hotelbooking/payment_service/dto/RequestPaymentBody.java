package com.hotelbooking.payment_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class RequestPaymentBody {
    private Long amount;
    private String orderId;
    private String currency;
}
