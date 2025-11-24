package com.hotelbooking.payment_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotelbooking.payment_service.domain.PaymentStatus;
import com.hotelbooking.payment_service.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    boolean existsByOrderId(String orderId);


     
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    Long sumAmountByStatus(@Param("status") PaymentStatus status);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    Long sumAmountByStatusAndDateRange(
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT DATE(p.createdAt) as date, SUM(p.amount) as revenue " +
           "FROM Payment p " +
           "WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(p.createdAt) " +
           "ORDER BY DATE(p.createdAt)")
    List<Object[]> findDailyRevenueByDateRange(
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p.paymentMethod, COUNT(p), SUM(p.amount) " +
           "FROM Payment p " +
           "WHERE p.status = :status " +
           "GROUP BY p.paymentMethod")
    List<Object[]> findPaymentMethodStatistics(@Param("status") PaymentStatus status);
    
    @Query("SELECT p.status, COUNT(p), SUM(p.amount) FROM Payment p GROUP BY p.status")
    List<Object[]> findRevenueGroupByStatus();
    
    Long countByStatus(PaymentStatus status);
}