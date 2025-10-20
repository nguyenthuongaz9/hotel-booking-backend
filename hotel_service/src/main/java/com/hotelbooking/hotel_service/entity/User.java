package com.hotelbooking.hotel_service.entity;

import java.time.LocalDateTime;

import com.hotelbooking.hotel_service.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data

public class User {

    private String id;


    private String name;

    private String email;

    private String password;

    private String phone;
    private String address;
    private String cccd;
    private UserRole role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}