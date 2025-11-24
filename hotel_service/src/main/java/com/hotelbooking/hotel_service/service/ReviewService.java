package com.hotelbooking.hotel_service.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.hotelbooking.hotel_service.domain.ReviewRequest;
import com.hotelbooking.hotel_service.domain.ReviewResponse;
import com.hotelbooking.hotel_service.dto.ReviewSummary;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest reviewRequest);
    List<ReviewResponse> getReviewsByRoomId(String roomId);
    List<ReviewResponse> getReviewsByUserId(String userId);
    ReviewResponse updateReview(Integer reviewId, ReviewRequest reviewRequest);
    void deleteReview(Integer reviewId);
    ReviewSummary getReviewSummary(String roomId);
    boolean hasUserReviewedRoom(String userId, String roomId);
    ReviewResponse getReviewByUserAndRoom(String userId, String roomId);
    ResponseEntity<?> getOverallAverageRating();
}