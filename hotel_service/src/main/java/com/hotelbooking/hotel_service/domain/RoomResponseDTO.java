package com.hotelbooking.hotel_service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.hotelbooking.hotel_service.dto.ImageDto;

import lombok.Data;

@Data
public class RoomResponseDTO {
    private String id;
    private String roomNumber;
    private List<ImageDto> images;
    private RoomType type;
    private BigDecimal pricePerNight;
    private String description;
    private String location;
    private Integer capacity;
    private List<Amenities> amenities;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewWithUser> reviews;
}