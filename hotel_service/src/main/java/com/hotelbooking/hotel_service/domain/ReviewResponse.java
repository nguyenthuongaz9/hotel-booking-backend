package com.hotelbooking.hotel_service.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewResponse {
    private Integer id;
    private String userId;
    private String roomId;
    private String roomName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}