package com.hotelbooking.order_service.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.hotelbooking.order_service.dto.PaymentStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order {
    

    @Id
    @GeneratedValue(generator= "uuid")
    @org.hibernate.annotations.GenericGenerator(name ="uuid", strategy="uuid2")
    @Column(name = "id", updatable= false, nullable = false)
    private String id;

    @Column(name="user_id")
    private String userId;

    @Column(name ="total_amount", nullable=false , precision=12, scale=2)
    private BigDecimal totalAmount;

    @Column(name="final_amount", nullable=false, precision=12, scale=2)
    private BigDecimal finalAmount;

    @Column(name="payment_status", nullable=false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name="payment_method", length=50)
    private String paymentMethod;


    @Column(name="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="paid_at")
    private LocalDateTime paidAt;

    @Column(name ="deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDiscount> discounts; 



}
