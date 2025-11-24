package com.hotelbooking.payment_service.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.payment_service.domain.PaymentStatus;
import com.hotelbooking.payment_service.dto.PaymentResponse;
import com.hotelbooking.payment_service.dto.RequestPaymentBody;
import com.hotelbooking.payment_service.service.PaymentService;


@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public PaymentResponse createPayment(@RequestBody RequestPaymentBody requestPaymentBody) throws Exception {
        return paymentService.createPaymentIntent(
            requestPaymentBody.getAmount(), 
            requestPaymentBody.getOrderId(), 
            requestPaymentBody.getCurrency()
        );
    }

    @PostMapping("/confirm/{paymentIntentId}")
    public Map<String, Object> confirmPayment(@PathVariable String paymentIntentId) {
        return paymentService.confirmPayment(paymentIntentId);
    }

    @GetMapping("/status/{orderId}")
    public Map<String, Object> getPaymentStatus(@PathVariable String orderId) {
        return paymentService.getPaymentStatusByOrderId(orderId);
    }

    @PostMapping("/webhook")
    public String handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String eventType = (String) payload.get("type");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> object = (Map<String, Object>) data.get("object");
            String paymentIntentId = (String) object.get("id");

            if ("payment_intent.succeeded".equals(eventType)) {
                paymentService.updatePaymentStatus(paymentIntentId, PaymentStatus.PAID);
            } else if ("payment_intent.payment_failed".equals(eventType)) {
                paymentService.updatePaymentStatus(paymentIntentId, PaymentStatus.FAILED);
            }

            return "Webhook handled";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }


     @GetMapping("/revenue-overview")
    public ResponseEntity<Map<String, Object>> getRevenueOverview() {
        try {
            Map<String, Object> overview = paymentService.getRevenueOverview();
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<Map<String, Object>> getRevenueTrend(
            @RequestParam(defaultValue = "month") String period) {
        try {
            Map<String, Object> trend = paymentService.getRevenueByTimePeriod(period);
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<Map<String, Object>> getPaymentMethodStatistics() {
        try {
            Map<String, Object> stats = paymentService.getPaymentMethodStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/revenue-by-status")
    public ResponseEntity<Map<String, Object>> getRevenueByStatus() {
        try {
            Map<String, Object> revenueByStatus = paymentService.getRevenueByStatus();
            return ResponseEntity.ok(revenueByStatus);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
   
}