package com.hotelbooking.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private String id;
    private String userId;
    private String roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private RoomResponse room;
    private UserResponse user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
