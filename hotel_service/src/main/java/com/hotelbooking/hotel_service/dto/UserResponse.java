package com.hotelbooking.hotel_service.dto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
   
    private String id;

    private String name;

    private String email;

    private String phone;
    private String address;
    private String cccd;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
