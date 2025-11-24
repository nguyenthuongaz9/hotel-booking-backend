package com.hotelbooking.user_service.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.hotelbooking.user_service.config.jwt.JwtUtils;
import com.hotelbooking.user_service.dto.UserRole;
import com.hotelbooking.user_service.mapper.UserMapper;
import com.hotelbooking.user_service.model.User;
import com.hotelbooking.user_service.payload.JwtResponse;
import com.hotelbooking.user_service.payload.LoginRequest;
import com.hotelbooking.user_service.payload.TokenRefreshRequest;
import com.hotelbooking.user_service.payload.TokenRefreshResponse;
import com.hotelbooking.user_service.payload.UserRequest;
import com.hotelbooking.user_service.payload.UserResponse;
import com.hotelbooking.user_service.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired 
    private UserMapper userMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<?> registerAndLogin(User user) {

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }


        if (userRepository.findUserByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("User already exists");
        }

 
        String rawPassword = user.getPassword();
        if (rawPassword == null || rawPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        user.setRole(UserRole.USER);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setCreatedAt(LocalDateTime.now());

  
        User savedUser = userRepository.save(user);

   
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), rawPassword));
        SecurityContextHolder.getContext().setAuthentication(authentication);

 
        UserResponse userResponse = userMapper.toUserResponse(savedUser);
        String accessToken = jwtUtils.generateJwtToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken, userResponse));
    }

    public ResponseEntity<?> updateUser(String id, UserRequest user) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        User existingUser = existingUserOpt.get();
        existingUser.setName(user.getName());
        existingUser.setPhone(user.getPhone());

        userRepository.save(existingUser);
        UserResponse userResponse = userMapper.toUserResponse(existingUser);
        return ResponseEntity.ok(userResponse);
    }

    public ResponseEntity<?> login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword() 
                ));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.generateJwtToken(loginRequest.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(loginRequest.getEmail());

        Optional<User> userOptional = userRepository.findUserByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOptional.get();
        UserResponse userResponse = userMapper.toUserResponse(user);
        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken, userResponse));
    }

    public ResponseEntity<?> refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        if (jwtUtils.validateJwtToken(requestRefreshToken)) {
            String email = jwtUtils.getEmailFromJwtToken(requestRefreshToken);
            String newAccessToken = jwtUtils.generateJwtToken(email);

            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, requestRefreshToken));
        }

        return ResponseEntity.status(401).body("Refresh token is invalid or expired");
    }

    public ResponseEntity<?> getCurrentUser(){
        try {
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           
           if(authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())){
            return ResponseEntity.status(401).body("User not authenticated");

           }

           String email = authentication.getName();
           Optional<User> userOptional = userRepository.findUserByEmail(email);
           if(userOptional.isEmpty()){
            return ResponseEntity.status(404).body("User not found");
           }

           User user = userOptional.get();


           UserResponse userResponse = userMapper.toUserResponse(user);
           return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving user information");
        }
    }

    public ResponseEntity<?> getAllUsers(Pageable pageable, String search) {
        try {
            Page<User> usersPage;
            
            if (search != null && !search.trim().isEmpty()) {
                usersPage = userRepository.findByNameContainingOrEmailContainingIgnoreCase(
                    search, search, pageable);
            } else {
                usersPage = userRepository.findAll(pageable);
            }
            
            Page<UserResponse> userResponses = usersPage.map(userMapper::toUserResponse);
            
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error retrieving users: " + e.getMessage());
        }
    }

    public ResponseEntity<?> updateUserAdmin(String id, UserRequest userRequest) {
        try {
            Optional<User> existingUserOpt = userRepository.findById(id);
            if (existingUserOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }
            
            User existingUser = existingUserOpt.get();
            
            if (userRequest.getName() != null) {
                existingUser.setName(userRequest.getName());
            }
            if (userRequest.getPhone() != null) {
                existingUser.setPhone(userRequest.getPhone());
            }
            if (userRequest.getRole() != null) {
                existingUser.setRole(userRequest.getRole());
            }
            
            existingUser.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(existingUser);
            
            UserResponse userResponse = userMapper.toUserResponse(updatedUser);
            return ResponseEntity.ok(userResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating user: " + e.getMessage());
        }
    }

    public ResponseEntity<?> deleteUser(String id) {
        try {
            Optional<User> existingUserOpt = userRepository.findById(id);
            if (existingUserOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }
            
            userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted successfully");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting user: " + e.getMessage());
        }
    }

    public ResponseEntity<?> getUserById(String id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }
            
            UserResponse userResponse = userMapper.toUserResponse(userOpt.get());
            return ResponseEntity.ok(userResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error retrieving user: " + e.getMessage());
        }
    }

    public ResponseEntity<?> countUsers() {
        try {
            long userCount = userRepository.count();
            return ResponseEntity.ok(userCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error counting users: " + e.getMessage());
        }
    }
}