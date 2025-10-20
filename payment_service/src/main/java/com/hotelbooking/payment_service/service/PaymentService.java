package com.hotelbooking.payment_service.service;

import com.hotelbooking.payment_service.domain.PaymentStatus;
import com.hotelbooking.payment_service.model.Payment;
import com.hotelbooking.payment_service.repository.PaymentRepository;
import com.hotelbooking.payment_service.model.*;
import com.hotelbooking.payment_service.repository.PaymentRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService{

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Map<String, Object> createPaymentIntent(Long amount, String orderId, String currency) throws Exception {
        // Tạo PaymentIntent bên Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount) // đơn vị: cent (100 = 1$)
                .setCurrency(currency)
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .currency(currency)
                .stripePaymentIntentId(intent.getId())
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("paymentId", payment.getId());
        return response;
    }

    public void updatePaymentStatus(String paymentIntentId, PaymentStatus status) {
        Payment payment = paymentRepository.findAll()
                .stream()
                .filter(p -> paymentIntentId.equals(p.getStripePaymentIntentId()))
                .findFirst()
                .orElseThrow();

        payment.setStatus(status);
        paymentRepository.save(payment);
    }
}
