package com.hotelbooking.order_service.repository;

import com.hotelbooking.order_service.dto.OrderStatus;
import com.hotelbooking.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by user
    List<Order> findByUserId(Long userId);

    // Find orders by room
    List<Order> findByRoomId(Long roomId);

    // Find orders by status
    List<Order> findByStatus(OrderStatus status);

    // Find orders by user and status
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    // Check if room is available for given date range
    @Query("SELECT COUNT(o) FROM Order o WHERE o.roomId = :roomId " +
            "AND o.status IN ('PENDING', 'CONFIRMED') " +
            "AND ((o.checkIn <= :checkOut AND o.checkOut >= :checkIn))")
    Long countConflictingOrders(@Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}
