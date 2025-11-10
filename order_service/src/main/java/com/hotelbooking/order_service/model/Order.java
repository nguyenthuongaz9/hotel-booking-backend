package com.hotelbooking.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hotelbooking.order_service.dto.OrderStatus;
import com.hotelbooking.order_service.dto.PaymentStatus;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String userId;
    
    @Column(name = "room_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private String roomId;
    
    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;
    
    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 100)
    private PaymentStatus paymentStatus;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}