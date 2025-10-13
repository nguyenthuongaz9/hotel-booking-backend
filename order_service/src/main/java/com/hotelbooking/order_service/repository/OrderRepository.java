package com.hotelbooking.order_service.repository;

import com.hotelbooking.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}