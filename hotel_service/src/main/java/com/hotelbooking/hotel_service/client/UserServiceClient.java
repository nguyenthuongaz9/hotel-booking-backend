package com.hotelbooking.hotel_service.client;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.hotelbooking.hotel_service.dto.UserResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
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
                .uri("/api/user/{id}", userId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Client error when fetching user {}: {}", userId, response.statusCode());
                    return Mono.error(new RuntimeException("User not found with id: " + userId));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Server error when fetching user {}: {}", userId, response.statusCode());
                    return Mono.error(new RuntimeException("User service is unavailable"));
                })
                .bodyToMono(UserResponse.class)
                .doOnSuccess(user -> log.info("Successfully fetched user: {}", user.getId()))
                .doOnError(error -> log.error("Error fetching user {}: {}", userId, error.getMessage()))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable -> {
                    log.warn("Failed to fetch user {}, returning fallback: {}", userId, throwable.getMessage());
                    return Mono.just(createFallbackUser(userId));
                });
    }

    public Mono<List<UserResponse>> getUsersByIds(List<String> userIds) {
        log.info("Fetching {} users individually from user-service", userIds.size());

        return Flux.fromIterable(userIds)
                .flatMap(this::getUserById)
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(throwable -> {
                    log.warn("Error fetching users individually: {}", throwable.getMessage());
                    return Mono.just(createFallbackUsers(userIds));
                });
    }

    private List<UserResponse> createFallbackUsers(List<String> userIds) {
        return userIds.stream()
                .map(this::createFallbackUser)
                .collect(java.util.stream.Collectors.toList());
    }

    private Mono<List<UserResponse>> fetchUsersIndividually(List<String> userIds) {
        return Flux.fromIterable(userIds)
                .flatMap(this::getUserById)
                .collectList();
    }

    private UserResponse createFallbackUser(String userId) {
        return UserResponse.builder()
                .id(userId)
            .name("Unknown User"    )

                .email("unknown@example.com")
                .build();
    }
}