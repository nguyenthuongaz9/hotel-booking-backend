package com.hotelbooking.order_service.controller;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;

import com.hotelbooking.order_service.service.OrderService;

import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;

 
    @PostMapping("/place")
    public Mono placeOrder(@RequestHeader("Authorization") String authHeader, @RequestBody String orderData) {
        return orderService.placeOrder(authHeader, orderData);
    
    }
    
}
