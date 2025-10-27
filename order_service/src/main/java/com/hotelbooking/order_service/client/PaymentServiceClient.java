package com.hotelbooking.order_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

import com.hotelbooking.order_service.dto.PaymentRequest;
import com.hotelbooking.order_service.dto.PaymentResponse;

import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Component
@Slf4j
public class PaymentServiceClient {

    private final WebClient webClient;

    @Autowired
    public PaymentServiceClient(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://payment-service")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<PaymentResponse> createPayment(PaymentRequest paymentRequest) {
        log.info("Creating payment for order: {}", paymentRequest.getOrderId());
        
        return webClient.post()
                .uri("/api/payments/create")
                .bodyValue(paymentRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Client error when creating payment: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Payment creation failed"));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Server error when creating payment: {}", response.statusCode());
                    return Mono.error(new RuntimeException("Payment service is unavailable"));
                })
                .bodyToMono(PaymentResponse.class)
                .doOnSuccess(payment -> log.info("Payment created successfully: {}", payment.getPaymentId()))
                .doOnError(error -> log.error("Error creating payment: {}", error.getMessage()))
                .timeout(Duration.ofSeconds(10));
    }

    public Mono<PaymentResponse> getPaymentStatus(String paymentId) {
        log.info("Getting payment status for: {}", paymentId);
        
        return webClient.get()
                .uri("/api/payments/{paymentId}/status", paymentId)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .timeout(Duration.ofSeconds(5));
    }
}