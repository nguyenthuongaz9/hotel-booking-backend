package com.hotelbooking.order_service.service;

import com.hotelbooking.order_service.dto.OrderRequest;
import com.hotelbooking.order_service.dto.OrderResponse;
import com.hotelbooking.order_service.dto.OrderStatus;
import com.hotelbooking.order_service.exception.OrderNotFoundException;
import com.hotelbooking.order_service.exception.RoomNotAvailableException;
import com.hotelbooking.order_service.model.Order;
import com.hotelbooking.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // Validate dates
        if (request.getCheckOut().isBefore(request.getCheckIn()) ||
                request.getCheckOut().isEqual(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        // Check if room is available
        Long conflictingOrders = orderRepository.countConflictingOrders(
                request.getRoomId(),
                request.getCheckIn(),
                request.getCheckOut());

        if (conflictingOrders > 0) {
            throw new RoomNotAvailableException("Room is not available for selected dates");
        }

        // Create order
        Order order = Order.builder()
                .userId(request.getUserId())
                .roomId(request.getRoomId())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .totalPrice(request.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return mapToResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        // Validate dates
        if (request.getCheckOut().isBefore(request.getCheckIn()) ||
                request.getCheckOut().isEqual(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        // Check if room is available (excluding current order)
        Long conflictingOrders = orderRepository.countConflictingOrders(
                request.getRoomId(),
                request.getCheckIn(),
                request.getCheckOut());

        // If the room is different or dates changed, check availability
        if (!order.getRoomId().equals(request.getRoomId()) ||
                !order.getCheckIn().equals(request.getCheckIn()) ||
                !order.getCheckOut().equals(request.getCheckOut())) {
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
        return mapToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        return mapToResponse(cancelledOrder);
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
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
