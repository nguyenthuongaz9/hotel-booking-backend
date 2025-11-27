package com.hotelbooking.order_service.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.hotelbooking.order_service.dto.RoomResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RoomServiceClient {

    private final WebClient webClient;

    @Autowired
    public RoomServiceClient(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://hotel-service")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<RoomResponse> getRoomById(String roomId) {
        log.info("Fetching room details for roomId: {} from hotel-service", roomId);

        return webClient.get()
                .uri("/api/rooms/{id}", roomId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Client error when fetching room {}: {}", roomId, response.statusCode());
                    return Mono.error(new RuntimeException("Room not found with id: " + roomId));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Server error when fetching room {}: {}", roomId, response.statusCode());
                    return Mono.error(new RuntimeException("Hotel service is unavailable"));
                })
                .bodyToMono(RoomResponse.class)
                .doOnSuccess(room -> log.info("Successfully fetched room: {}", room.getId()))
                .doOnError(error -> log.error("Error fetching room {}: {}", roomId, error.getMessage()))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable -> {
                    log.warn("Failed to fetch room {}, returning fallback: {}", roomId, throwable.getMessage());
                    return Mono.just(createFallbackRoom(roomId));
                });
    }

    public Mono<Boolean> isAvailable(String roomId) {
        log.info("Fetching room availability for roomId: {} from hotel-service", roomId);

        return webClient.get()
                .uri("/api/rooms/{id}/isAvailable", roomId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Client error when fetching room {}: {}", roomId, response.statusCode());
                    return Mono.error(new RuntimeException("Room not found with id: " + roomId));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Server error when fetching room {}: {}", roomId, response.statusCode());
                    return Mono.error(new RuntimeException("Hotel service is unavailable"));
                })
                .bodyToMono(Boolean.class) 
                .doOnSuccess(available -> log.info("Room {} availability: {}", roomId, available))
                .doOnError(error -> log.error("Error fetching room {} availability: {}", roomId, error.getMessage()))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable -> {
                    log.warn("Failed to fetch room {} availability, returning fallback", roomId);
                    return Mono.just(false);
                });
    }

    private RoomResponse createFallbackRoom(String roomId) {
        return RoomResponse.builder()
                .id(roomId)
                .roomNumber("Unknown")
                .type("UNKNOWN")
                .pricePerNight(BigDecimal.ZERO)
                .capacity(0)
                .description("Room information temporarily unavailable")
                .isAvailable(false)
                .location("Unknown")
                .amenities(List.of())
                .images(List.of())
                .build();
    }
}