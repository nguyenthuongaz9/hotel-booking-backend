package com.hotelbooking.hotel_service.domain;

import java.time.LocalDateTime;

import com.hotelbooking.hotel_service.dto.UserResponse;

import lombok.Data;

@Data
public class ReviewWithUser {
    private Integer id;
    private String userId;
    private UserResponse user;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}