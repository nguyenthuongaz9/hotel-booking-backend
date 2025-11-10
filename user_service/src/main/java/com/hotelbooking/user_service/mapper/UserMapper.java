package com.hotelbooking.user_service.mapper;

import org.springframework.stereotype.Component;

import com.hotelbooking.user_service.model.User;
import com.hotelbooking.user_service.payload.UserResponse;


@Component
public class UserMapper {
    
    public UserResponse toUserResponse(User user){
        if(user == null){
            return null;
        }

        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getAddress(),
            user.getCccd(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
