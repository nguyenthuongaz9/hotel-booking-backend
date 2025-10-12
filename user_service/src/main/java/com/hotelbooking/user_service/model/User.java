package com.hotelbooking.user_service.model;

import java.time.LocalDateTime;

import com.hotelbooking.user_service.dto.UserRole;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Document(collection="user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
   
    @Id()
    private String id;


    private String name;

    @Indexed(unique= true)
    private String email;

    private String password;

    private String phone;
    private String address;
    private String cccd;
    private UserRole role;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
