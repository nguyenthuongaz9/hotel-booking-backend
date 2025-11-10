package com.hotelbooking.payment_service.repository;

import com.hotelbooking.payment_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    boolean existsByOrderId(String orderId);
}