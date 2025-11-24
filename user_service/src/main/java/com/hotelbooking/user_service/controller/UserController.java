package com.hotelbooking.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.user_service.model.User;
import com.hotelbooking.user_service.payload.LoginRequest;
import com.hotelbooking.user_service.payload.TokenRefreshRequest;
import com.hotelbooking.user_service.payload.UserRequest;
import com.hotelbooking.user_service.service.UserService;

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

    @PatchMapping("/api/user/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable("id") String id,
            @RequestBody UserRequest user) {
        return userService.updateUser(id, user);
    }

    @GetMapping("/api/user/me")
    public ResponseEntity<?> getMethodName() {
        return userService.getCurrentUser();
    }

    @GetMapping("/api/user")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search) {

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userService.getAllUsers(pageable, search);
    }

    @GetMapping("/api/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @PutMapping("/api/user/{id}/admin")
    public ResponseEntity<?> updateUserAdmin(@PathVariable String id, @RequestBody UserRequest userRequest) {
        return userService.updateUserAdmin(id, userRequest);
    }

    @DeleteMapping("/api/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        return userService.deleteUser(id);
    }

    @GetMapping("/api/user/count")
    public ResponseEntity<?> countUsers() {
        return userService.countUsers();
    }

}
