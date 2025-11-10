package com.hotelbooking.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.user_service.model.User;
import com.hotelbooking.user_service.payload.LoginRequest;
import com.hotelbooking.user_service.payload.TokenRefreshRequest;
import com.hotelbooking.user_service.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hotelbooking.user_service.payload.UserResponse;


@RestController
@RequestMapping("")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        return userService.registerAndLogin(user);
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/api/auth/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        return userService.refreshToken(request);
    }

    @GetMapping("/api/user/me")
    public ResponseEntity<?> getMethodName() {
        return userService.getCurrentUser();
    }
    

    
}
