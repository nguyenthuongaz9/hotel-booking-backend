package com.hotelbooking.payment_service.repository;

import com.hotelbooking.payment_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String> {

}

