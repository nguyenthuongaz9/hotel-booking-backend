package com.hotelbooking.payment_service.controller;


import com.hotelbooking.payment_service.domain.PaymentStatus;
import com.hotelbooking.payment_service.dto.RequestPaymentBody;
import com.hotelbooking.payment_service.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {


    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
            this.paymentService = paymentService;
    }


    @PostMapping("/create")
    public Map<String, Object> createPayment(
            @RequestBody RequestPaymentBody requestPaymentBody

            ) throws Exception {
        return paymentService.createPaymentIntent(requestPaymentBody.getAmount(), requestPaymentBody.getOrderId(), requestPaymentBody.getCurrency());
    }

    @PostMapping("/webhook")
    public String handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String eventType = (String) payload.get("type");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> object = (Map<String, Object>) data.get("object");
            String paymentIntentId = (String) object.get("id");

            if ("payment_intent.succeeded".equals(eventType)) {
                paymentService.updatePaymentStatus(paymentIntentId, PaymentStatus.SUCCESS);
            } else if ("payment_intent.payment_failed".equals(eventType)) {
                paymentService.updatePaymentStatus(paymentIntentId, PaymentStatus.FAILED);
            }

            return "Webhook handled";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
}
