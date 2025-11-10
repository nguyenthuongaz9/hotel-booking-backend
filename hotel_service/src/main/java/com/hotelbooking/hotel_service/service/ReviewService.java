package com.hotelbooking.hotel_service.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hotelbooking.hotel_service.client.UserServiceClient;
import com.hotelbooking.hotel_service.dto.PaginatedReviewResponse;
import com.hotelbooking.hotel_service.dto.ReviewResponseDto;
import com.hotelbooking.hotel_service.model.Review;
import com.hotelbooking.hotel_service.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserServiceClient userServiceClient;

    
    private ReviewResponseDto mappingResponseDto(Review review) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setRoom(review.getRoom());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    @Cacheable(value = "topReviews", key = "'homepage'")
    public ResponseEntity<List<ReviewResponseDto>> getTopReviewsForHomepage() {
        try {
            Thread.sleep(1000);

            Pageable pageable = PageRequest.of(0, 3);
            Page<Review> topReviews = reviewRepository.findTopByOrderByRatingDesc(pageable);

            List<CompletableFuture<ReviewResponseDto>> futures = topReviews.getContent().stream()
                    .map(review -> {
                        ReviewResponseDto reviewDto = mappingResponseDto(review);

                        return userServiceClient.getUserById(review.getUserId())
                                .map(user -> {
                                    reviewDto.setUser(user); 
                                    return reviewDto;
                                })
                                .onErrorReturn(reviewDto)
                                .toFuture();
                    })
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<ReviewResponseDto> reviewDtos = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(reviewDtos);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}