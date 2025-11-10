package com.hotelbooking.user_service.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
