package com.hotelbooking.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.hotelbooking.user_service.config.jwt.JwtUtils;
import com.hotelbooking.user_service.model.User;
import com.hotelbooking.user_service.payload.JwtResponse;
import com.hotelbooking.user_service.payload.LoginRequest;
import com.hotelbooking.user_service.payload.TokenRefreshRequest;
import com.hotelbooking.user_service.payload.TokenRefreshResponse;
import com.hotelbooking.user_service.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

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
        user.setPassword(passwordEncoder.encode(rawPassword));

  
        User savedUser = userRepository.save(user);

   
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), rawPassword));
        SecurityContextHolder.getContext().setAuthentication(authentication);

 
        String accessToken = jwtUtils.generateJwtToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
    }

    public ResponseEntity<?> login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword() // password raw
                ));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.generateJwtToken(loginRequest.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(loginRequest.getEmail());

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
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
}
