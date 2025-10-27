package com.hotelbooking.hotel_service.dto;

import com.hotelbooking.hotel_service.domain.Amenities;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequestDto {
        private String roomNumber;
        private String type;
        private BigDecimal pricePerNight;
        private String location;
        private Integer capacity;
        private String description;
        private List<Amenities> amenities;
        private Boolean isAvailable;
}