package com.hotelbooking.hotel_service.client;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.hotelbooking.hotel_service.dto.UserResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class UserServiceClient {

    private final WebClient webClient;

    @Autowired
    public UserServiceClient(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://user-service") 
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<UserResponse> getUserById(String userId) {
        log.info("Fetching user details for userId: {} from user-service", userId);
        
        return webClient.get()
                .uri("/api/users/{id}", userId) // Sửa URI endpoint
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Client error when fetching user {}: {}", userId, response.statusCode());
                    return Mono.error(new RuntimeException("User not found with id: " + userId));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Server error when fetching user {}: {}", userId, response.statusCode());
                    return Mono.error(new RuntimeException("User service is unavailable"));
                })
                .bodyToMono(UserResponse.class) // Sửa thành UserResponse
                .doOnSuccess(user -> log.info("Successfully fetched user: {}", user.getId()))
                .doOnError(error -> log.error("Error fetching user {}: {}", userId, error.getMessage()))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable -> {
                    log.warn("Failed to fetch user {}, returning fallback: {}", userId, throwable.getMessage());
                    return Mono.just(createFallbackUser(userId)); // Sửa thành createFallbackUser
                });
    }

    private UserResponse createFallbackUser(String userId) {
        return UserResponse.builder()
                .id(userId)
                .name("Unknown User")
                .email("unknown@example.com")
                .phone("N/A")
                .address("N/A")
                .cccd("N/A")
                .build();
    }
}