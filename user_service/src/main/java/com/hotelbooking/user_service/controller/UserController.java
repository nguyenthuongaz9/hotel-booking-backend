package com.hotelbooking.user_service.controller;


import com.hotelbooking.user_service.model.User;
import com.hotelbooking.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class UserController {


    @Autowired
    private UserService userService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody User user){
        try{
           User savedUser = userService.register(user);
           return ResponseEntity.ok("register successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        boolean success = userService.login(user.getEmail(), user.getPassword());
        if (success) {
            return ResponseEntity.ok("Đăng nhập thành công!");
        } else {
            return ResponseEntity.status(401).body("Tên đăng nhập hoặc mật khẩu không đúng");
        }
    }


}
