package com.hotelbooking.order_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.order_service.dto.OrderRequest;
import com.hotelbooking.order_service.dto.OrderResponse;
import com.hotelbooking.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.hotelbooking.order_service.dto.OrderStatus;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/user/{userId}/with-rooms")
    public Flux<OrderResponse> getOrdersByUserIdWithRooms(@PathVariable String userId) {
        return orderService.getOrdersByUserIdWithRoomInfo(userId);
    }

    @GetMapping("/user/{userId}/with-rooms-async")
    public CompletableFuture<ResponseEntity<List<OrderResponse>>> getOrdersByUserIdWithRoomsAsync(@PathVariable String userId) {
        return orderService.getOrdersByUserIdWithRoomInfoAsync(userId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/user/{userId}/with-rooms-sync")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserIdWithRoomsSync(@PathVariable String userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserIdWithRoomInfoSync(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}/with-room")
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
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
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
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> request) {
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
}