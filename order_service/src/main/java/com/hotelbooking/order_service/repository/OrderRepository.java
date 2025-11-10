package com.hotelbooking.order_service.repository;

import com.hotelbooking.order_service.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.hotelbooking.order_service.dto.OrderStatus;
import com.hotelbooking.order_service.dto.PaymentStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    List<Order> findByUserId(String userId);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.roomId = :roomId " +
           "AND o.status NOT IN (CANCELLED, COMPLETED) " +
           "AND ((o.checkIn <= :checkOut AND o.checkOut >= :checkIn))")
    Long countConflictingOrders(@Param("roomId") String roomId, 
                               @Param("checkIn") LocalDate checkIn, 
                               @Param("checkOut") LocalDate checkOut);
    
    @Query("SELECT o FROM Order o WHERE o.roomId = :roomId " +
           "AND o.status NOT IN (CANCELLED, COMPLETED) " +
           "ORDER BY o.checkIn ASC")
    List<Order> findActiveOrdersByRoomId(@Param("roomId") String roomId);
}