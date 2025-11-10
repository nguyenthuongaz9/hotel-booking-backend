package com.hotelbooking.hotel_service.dto;
import lombok.*;
import java.time.LocalDateTime;
import com.hotelbooking.hotel_service.model.Room;

@Data
public class ReviewResponseDto {
    private Integer id;
    private String userId;
    private Room room;
    private Integer rating;
    private String comment;
    private UserResponse user;
    private LocalDateTime createdAt;

}

