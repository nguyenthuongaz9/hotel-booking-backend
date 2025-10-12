package com.hotelbooking.user_service.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hotelbooking.user_service.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findUserById(String id);
    Optional<User> findUserByEmail(String email);
    
}
