package com.hotelbooking.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusOrderRequest {
    private String userEmail;
    private String status;
    private String roomNumber;
    private String checkIn;
    private String checkOut;
    private BigDecimal totalAmount;
}
