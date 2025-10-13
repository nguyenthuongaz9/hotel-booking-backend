package com.hotelbooking.order_service.config.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import com.hotelbooking.order_service.payload.UserResponse;

import reactor.core.publisher.Mono;

public class UserClient {
    
    @Autowired
    private WebClient.Builder webClientBuilder;


    public Mono<UserResponse> getCurrentUser(String token){
        return webClientBuilder.build()
        .get()
        .uri("http://user_service/api/auth/me")
        .header("Authorization", "Bearer" + token)
        .retrieve()
        .bodyToMono(UserResponse.class);
    }
}
