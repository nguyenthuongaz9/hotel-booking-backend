package com.hotelbooking.order_service.service;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.order_service.client.PaymentServiceClient;
import com.hotelbooking.order_service.client.RoomServiceClient;
import com.hotelbooking.order_service.client.UserServiceClient;
import com.hotelbooking.order_service.dto.OrderRequest;
import com.hotelbooking.order_service.dto.OrderResponse;
import com.hotelbooking.order_service.dto.OrderStatus;
import com.hotelbooking.order_service.dto.PaginatedOrderResponse;
import com.hotelbooking.order_service.dto.PaymentRequest;
import com.hotelbooking.order_service.dto.PaymentResponse;
import com.hotelbooking.order_service.dto.PaymentStatus;
import com.hotelbooking.order_service.dto.RoomResponse;
import com.hotelbooking.order_service.dto.UserResponse;
import com.hotelbooking.order_service.exception.OrderNotFoundException;
import com.hotelbooking.order_service.exception.RoomNotAvailableException;
import com.hotelbooking.order_service.model.Order;
import com.hotelbooking.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RoomServiceClient roomServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final UserServiceClient userServiceClient;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for user: {}, room: {}", request.getUserId(), request.getRoomId());
        if (request.getCheckOut().isBefore(request.getCheckIn()) ||
                request.getCheckOut().isEqual(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        Long conflictingOrders = orderRepository.countConflictingOrders(
                request.getRoomId(),
                request.getCheckIn(),
                request.getCheckOut());

        if (conflictingOrders > 0) {
            throw new RoomNotAvailableException("Room is not available for selected dates");
        }
        Order order = Order.builder()
                .userId(request.getUserId())
                .roomId(request.getRoomId())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .totalPrice(request.getTotalPrice())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return mapToResponse(savedOrder);
    }

    @Transactional
    public PaymentResponse createPaymentSession(String orderId, String successUrl, String cancelUrl) {
        log.info("Creating payment session for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new IllegalStateException("Order payment status is not UNPAID");
        }

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(orderId)
                .amount(order.getTotalPrice())
                .currency("USD")
                .paymentMethod("STRIPE")
                .successUrl(successUrl)
                .cancelUrl(cancelUrl)
                .customerEmail("customer@example.com")
                .build();

        PaymentResponse paymentResponse = paymentServiceClient.createPayment(paymentRequest)
                .block(Duration.ofSeconds(10));

        order.setPaymentStatus(PaymentStatus.PENDING);
        orderRepository.save(order);

        log.info("Payment session created successfully for order: {}", orderId);
        return paymentResponse;
    }

    @Transactional
    public void handlePaymentSuccess(String orderId) {
        log.info("Handling payment success for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        log.info("Order confirmed after successful payment: {}", orderId);
    }

    @Transactional
    public void handlePaymentFailure(String orderId) {
        log.info("Handling payment failure for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);

        log.info("Order payment marked as failed: {}", orderId);
    }

    public Flux<OrderResponse> getOrdersByUserIdWithRoomInfo(String userId) {
        log.info("Fetching orders with room info for user: {}", userId);

        return Flux.fromIterable(orderRepository.findByUserId(userId))
                .flatMap(order -> {
                    OrderResponse orderResponse = mapToResponse(order);

                    return roomServiceClient.getRoomById(order.getRoomId())
                            .map(room -> {
                                orderResponse.setRoom(room);
                                return orderResponse;
                            })
                            .onErrorReturn(orderResponse);
                })
                .doOnNext(orderResponse -> log.info("Processed order: {}", orderResponse.getId()))
                .doOnError(error -> log.error("Error processing orders for user {}: {}", userId, error.getMessage()));
    }

    public CompletableFuture<List<OrderResponse>> getOrdersByUserIdWithRoomInfoAsync(String userId) {
        log.info("Fetching orders with room info async for user: {}", userId);

        List<Order> orders = orderRepository.findByUserId(userId);

        List<CompletableFuture<OrderResponse>> futures = orders.stream()
                .map(order -> {
                    OrderResponse orderResponse = mapToResponse(order);

                    return roomServiceClient.getRoomById(order.getRoomId())
                            .map(room -> {
                                orderResponse.setRoom(room);
                                return orderResponse;
                            })
                            .onErrorReturn(orderResponse)
                            .toFuture();
                })
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    public List<OrderResponse> getOrdersByUserIdWithRoomInfoSync(String userId) {
        log.info("Fetching orders with room info sync for user: {}", userId);

        List<Order> orders = orderRepository.findByUserId(userId);
        List<OrderResponse> responses = new java.util.ArrayList<>();

        for (Order order : orders) {
            OrderResponse orderResponse = mapToResponse(order);

            try {
                RoomResponse room = roomServiceClient.getRoomById(order.getRoomId())
                        .block(Duration.ofSeconds(5));
                orderResponse.setRoom(room);
            } catch (Exception e) {
                log.warn("Failed to fetch room info for order {}: {}", order.getId(), e.getMessage());
            }

            responses.add(orderResponse);
        }

        return responses;
    }

    public OrderResponse getOrderById(String id) {
        log.info("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    public OrderResponse getOrderByIdWithRoomInfo(String id) {
        log.info("Fetching order with room info by ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        OrderResponse orderResponse = mapToResponse(order);

        try {
            RoomResponse room = roomServiceClient.getRoomById(order.getRoomId())
                    .block(Duration.ofSeconds(5));
            orderResponse.setRoom(room);
        } catch (Exception e) {
            log.warn("Failed to fetch room info for order {}: {}", id, e.getMessage());
        }

        return orderResponse;
    }

      public PaginatedOrderResponse getAllOrders(int page, int size) {
        log.info("Fetching all orders with pagination - page: {}, size: {}", page, size);

        if (page < 0) {
            throw new IllegalArgumentException("Page number must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAll(pageable);

        List<CompletableFuture<OrderResponse>> futures = orderPage.getContent().stream()
                .map(order -> {
                    OrderResponse orderResponse = mapToResponse(order);
                    
                    CompletableFuture<RoomResponse> roomFuture = roomServiceClient.getRoomById(order.getRoomId())
                            .toFuture();
                    
                    CompletableFuture<UserResponse> userFuture = userServiceClient.getUserById(order.getUserId())
                            .toFuture();
                    
                    return CompletableFuture.allOf(roomFuture, userFuture)
                            .thenApply(v -> {
                                try {
                                    RoomResponse room = roomFuture.get();
                                    UserResponse user = userFuture.get();
                                    
                                    orderResponse.setRoom(room);
                                    if (user != null) {
                                        orderResponse.setUser(user);
                                    }
                                } catch (Exception e) {
                                    log.warn("Error setting room/user info for order {}: {}", order.getId(), e.getMessage());
                                }
                                return orderResponse;
                            });
                })
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<OrderResponse> orderResponses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        Page<OrderResponse> orderResponsePage = new PageImpl<>(
                orderResponses,
                pageable,
                orderPage.getTotalElements());

        log.info("Retrieved {} orders with room and user info", orderResponses.size());

        return PaginatedOrderResponse.fromPage(orderResponsePage);
    }



    public List<OrderResponse> getOrdersByUserId(String userId) {
        log.info("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.info("Fetching orders by status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByPaymentStatus(PaymentStatus paymentStatus) {
        log.info("Fetching orders by payment status: {}", paymentStatus);
        return orderRepository.findByPaymentStatus(paymentStatus).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrder(String id, OrderRequest request) {
        log.info("Updating order: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (request.getCheckOut().isBefore(request.getCheckIn()) ||
                request.getCheckOut().isEqual(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (!order.getRoomId().equals(request.getRoomId()) ||
                !order.getCheckIn().equals(request.getCheckIn()) ||
                !order.getCheckOut().equals(request.getCheckOut())) {

            Long conflictingOrders = orderRepository.countConflictingOrders(
                    request.getRoomId(),
                    request.getCheckIn(),
                    request.getCheckOut());

            if (conflictingOrders > 0) {
                throw new RoomNotAvailableException("Room is not available for selected dates");
            }
        }

        order.setUserId(request.getUserId());
        order.setRoomId(request.getRoomId());
        order.setCheckIn(request.getCheckIn());
        order.setCheckOut(request.getCheckOut());
        order.setTotalPrice(request.getTotalPrice());

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated successfully: {}", id);
        return mapToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(String id, OrderStatus status) {
        log.info("Updating order status: {} to {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: {} to {}", id, status);
        return mapToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse updatePaymentStatus(String id, String paymentStatusJson) {
        log.info("Updating payment status: {} to {}", id, paymentStatusJson);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(paymentStatusJson);
            String statusValue = jsonNode.get("status").asText();

            PaymentStatus newPaymentStatus = PaymentStatus.valueOf(statusValue.toUpperCase());
            log.info("Converting payment status: {} to {}", statusValue, newPaymentStatus);

            order.setPaymentStatus(newPaymentStatus);

            if (newPaymentStatus == PaymentStatus.PAID && order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
                log.info("Order {} auto-confirmed after payment", id);
            }

            Order updatedOrder = orderRepository.save(order);
            log.info("Payment status updated successfully: {} to {}", id, newPaymentStatus);
            return mapToResponse(updatedOrder);

        } catch (IOException e) {
            log.error("Invalid JSON format: {}", paymentStatusJson);
            throw new IllegalArgumentException("Invalid JSON format: " + paymentStatusJson);
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment status in JSON: {}", paymentStatusJson);
            throw new IllegalArgumentException("Invalid payment status in JSON: " + paymentStatusJson);
        }
    }

    @Transactional
    public void deleteOrder(String id) {
        log.info("Deleting order: {}", id);
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
        log.info("Order deleted successfully: {}", id);
    }

    @Transactional
    public OrderResponse cancelOrder(String id) {
        log.info("Cancelling order: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled successfully: {}", id);
        return mapToResponse(cancelledOrder);
    }

    public boolean isRoomAvailable(String roomId, java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        log.info("Checking room availability: {} from {} to {}", roomId, checkIn, checkOut);
        Long conflictingOrders = orderRepository.countConflictingOrders(roomId, checkIn, checkOut);
        return conflictingOrders == 0;
    }

    public List<OrderResponse> getActiveOrdersByRoomId(String roomId) {
        log.info("Fetching active orders for room: {}", roomId);
        return orderRepository.findActiveOrdersByRoomId(roomId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .roomId(order.getRoomId())
                .checkIn(order.getCheckIn())
                .checkOut(order.getCheckOut())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public ResponseEntity<?> countOrders() {
        log.info("Counting total orders");
        return ResponseEntity.ok(orderRepository.count());
    }
}