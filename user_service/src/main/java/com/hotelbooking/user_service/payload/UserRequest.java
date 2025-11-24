package com.hotelbooking.user_service.payload;

import com.hotelbooking.user_service.dto.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String name;

    private String email;

    private String phone;
    private String address;
    private String cccd;
    private UserRole role;

}
