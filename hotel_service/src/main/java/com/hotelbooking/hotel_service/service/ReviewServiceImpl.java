package com.hotelbooking.hotel_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel_service.domain.ReviewRequest;
import com.hotelbooking.hotel_service.domain.ReviewResponse;
import com.hotelbooking.hotel_service.dto.ReviewSummary;
import com.hotelbooking.hotel_service.model.Review;
import com.hotelbooking.hotel_service.model.Room;
import com.hotelbooking.hotel_service.repository.ReviewRepository;
import com.hotelbooking.hotel_service.repository.RoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest reviewRequest) {
        Room room = roomRepository.findById(reviewRequest.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + reviewRequest.getRoomId()));

        if (reviewRepository.existsByUserIdAndRoomId(reviewRequest.getUserId(), reviewRequest.getRoomId())) {
            throw new RuntimeException("User has already reviewed this room");
        }

        Review review = new Review();
        review.setUserId(reviewRequest.getUserId());
        review.setRoom(room);
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());

        Review savedReview = reviewRepository.save(review);
        return mapToReviewResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> getReviewsByRoomId(String roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RuntimeException("Room not found with id: " + roomId);
        }

        return reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId)
                .stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByUserId(String userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Integer reviewId, ReviewRequest reviewRequest) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        if (!review.getUserId().equals(reviewRequest.getUserId())) {
            throw new RuntimeException("User is not authorized to update this review");
        }

        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());

        Review updatedReview = reviewRepository.save(review);
        return mapToReviewResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        reviewRepository.delete(review);
    }

    @Override
    public ReviewSummary getReviewSummary(String roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RuntimeException("Room not found with id: " + roomId);
        }

        Double averageRating = reviewRepository.findAverageRatingByRoomId(roomId).orElse(0.0);
        Long totalReviews = reviewRepository.countByRoomId(roomId);

        ReviewSummary summary = new ReviewSummary();
        summary.setAverageRating(Math.round(averageRating * 10.0) / 10.0);
        summary.setTotalReviews(totalReviews.intValue());
        summary.setRating5(reviewRepository.countByRoomIdAndRating(roomId, 5).intValue());
        summary.setRating4(reviewRepository.countByRoomIdAndRating(roomId, 4).intValue());
        summary.setRating3(reviewRepository.countByRoomIdAndRating(roomId, 3).intValue());
        summary.setRating2(reviewRepository.countByRoomIdAndRating(roomId, 2).intValue());
        summary.setRating1(reviewRepository.countByRoomIdAndRating(roomId, 1).intValue());

        return summary;
    }

    @Override
    public boolean hasUserReviewedRoom(String userId, String roomId) {
        return reviewRepository.existsByUserIdAndRoomId(userId, roomId);
    }

    @Override
    public ReviewResponse getReviewByUserAndRoom(String userId, String roomId) {
        return reviewRepository.findByUserIdAndRoomId(userId, roomId)
                .map(this::mapToReviewResponse)
                .orElse(null);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUserId());
        response.setRoomId(review.getRoom().getId());
        response.setRoomName("Room " + review.getRoom().getRoomNumber());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }

    public ResponseEntity<?> getOverallAverageRating() {
        return ResponseEntity.ok(reviewRepository.findOverallAverageRating());
    }

}