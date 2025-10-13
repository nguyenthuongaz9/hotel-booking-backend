package com.hotelbooking.order_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.hotelbooking.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.hotelbooking.order_service.model.Order;
import com.hotelbooking.order_service.model.OrderDiscount;
import com.hotelbooking.order_service.model.OrderItem;
import lombok.RequiredArgsConstructo;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private final WebClient.Builder webClientBuilder;

    private final OrderRepository orderRepository;

    public Mono<String> placeOrder(String authHeader, String orderData) {
        String userServiceUrl = "http://user-service/api/user/current";

        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl)
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(userJson -> {
                    String userId = extractUserId(userJson);

                    Order order = new Order();
                    order.setUserId(userId);
                    order.setTotalAmount(orderData.getTotalAmount());
                    order.setFinalAmount(orderData.getFinalAmount());
                    order.setPaymentMethod(orderData.getPaymentMethod());
                    order.setCreatedAt(LocalDateTime.now());

                    var items = orderData.getItems().stream()
                            .map(i -> OrderItem.builder()
                                    .order(order)
                                    .zoneId(i.getZoneId())
                                    .ticketTypeId(i.getTicketTypeId())
                                    .quantity(i.getQuantity())
                                    .unitPrice(i.getUnitPrice())
                                    .subtotal(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                                    .build())
                            .collect(Collectors.toList());
                    order.setItems(items);

                    var discounts = orderData.getDiscounts().stream()
                            .map(d -> OrderDiscount.builder()
                                    .order(order)
                                    .discountId(d.getDiscountId())
                                    .appliedValue(d.getAppliedValue())
                                    .createdAt(LocalDateTime.now())
                                    .build())
                            .collect(Collectors.toList());
                    order.setDiscounts(discounts);

                    orderRepository.save(order);
                    return Mono.just(" Đặt hàng thành công cho user ID: " + userId);
                })
                .onErrorResume(e -> Mono.just(" Lỗi khi đặt hàng: " + e.getMessage()));
    }


     private String extractUserId(String userJson) {
       
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readTree(userJson).get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc userId từ user_service: " + e.getMessage());
        }
    }
}
