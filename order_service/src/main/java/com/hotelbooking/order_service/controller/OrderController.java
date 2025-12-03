package com.hotelbooking.order_service.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.hibernate.sql.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.order_service.dto.OrderRequest;
import com.hotelbooking.order_service.dto.OrderResponse;
import com.hotelbooking.order_service.dto.OrderStatus;
import com.hotelbooking.order_service.dto.PaginatedOrderResponse;
import com.hotelbooking.order_service.dto.UpdateStatusOrderRequest;
import com.hotelbooking.order_service.exception.ErrorResponse;
import com.hotelbooking.order_service.exception.RoomNotAvailableException;
import com.hotelbooking.order_service.service.OrderService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Mono<ResponseEntity<?>> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Received order creation request: {}", request);

        return orderService.createOrder(request)
                .<ResponseEntity<?>>map(orderResponse -> {
                    log.info("Order created successfully: {}", orderResponse.getId());
                    return ResponseEntity.ok(orderResponse);
                })
                .onErrorResume(throwable -> {
                    log.error("=== ORDER CREATION ERROR ===");
                    log.error("Error type: {}", throwable.getClass().getName());
                    log.error("Error message: {}", throwable.getMessage());

                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setMessage(throwable.getMessage());
                    errorResponse.setTimestamp(LocalDateTime.now());
                    errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
                    errorResponse.setError("Bad Request");

                    if (throwable instanceof RoomNotAvailableException) {
                        log.warn("Room not available: {}", throwable.getMessage());
                        errorResponse.setStatus(HttpStatus.CONFLICT.value());
                        errorResponse.setError("Conflict");
                        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
                    } else if (throwable instanceof IllegalArgumentException) {
                        log.warn("Invalid request: {}", throwable.getMessage());
                        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                    } else {
                        log.error("Internal server error: {}", throwable.getMessage());
                        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                        errorResponse.setError("Internal Server Error");
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                    }
                });
    }

    @GetMapping("/user/{userId}/with-rooms")
    @CircuitBreaker(name = "hotel-service", fallbackMethod = "fallbackMethodWithUserId")
    public Flux<OrderResponse> getOrdersByUserIdWithRooms(@PathVariable String userId) {
        return orderService.getOrdersByUserIdWithRoomInfo(userId);
    }

    @GetMapping("/user/{userId}/with-rooms-async")
    @CircuitBreaker(name = "hotel-service", fallbackMethod = "fallbackMethodWithUserId")
    public CompletableFuture<ResponseEntity<List<OrderResponse>>> getOrdersByUserIdWithRoomsAsync(
            @PathVariable String userId) {
        return orderService.getOrdersByUserIdWithRoomInfoAsync(userId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}/with-room")
    @CircuitBreaker(name = "hotel-service", fallbackMethod = "fallbackMethodWithUserId")
    public ResponseEntity<OrderResponse> getOrderByIdWithRoom(@PathVariable String id) {
        OrderResponse order = orderService.getOrderByIdWithRoomInfo(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable String userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<PaginatedOrderResponse> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PaginatedOrderResponse response = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/payment-status")
    public OrderResponse updatePaymentStatus(@PathVariable String id, @RequestBody UpdateStatusOrderRequest request) {
        OrderResponse order = orderService.updatePaymentStatus(id, request);
        return order;

    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String id, @RequestBody OrderRequest request) {
        OrderResponse order = orderService.updateOrder(id, request);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable String id,
            @RequestBody Map<String, String> request) {
        OrderStatus status = OrderStatus.valueOf(request.get("status"));
        OrderResponse order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String id) {
        OrderResponse order = orderService.cancelOrder(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Boolean>> checkRoomAvailability(
            @RequestParam String roomId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {

        java.time.LocalDate checkInDate = java.time.LocalDate.parse(checkIn);
        java.time.LocalDate checkOutDate = java.time.LocalDate.parse(checkOut);

        boolean isAvailable = orderService.isRoomAvailable(roomId, checkInDate, checkOutDate);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    public String fallbackMethodWithUserId(String userId, RuntimeException runtimeException) {

        return "Opps! Something went wrong, please order after some time!";
    }

    @GetMapping("/count")
    public ResponseEntity<?> countOrders() {
        log.info("Counting total orders");
        return orderService.countOrders();
    }
}