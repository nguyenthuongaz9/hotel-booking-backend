package com.hotelbooking.hotel_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotelbooking.hotel_service.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByRoomIdOrderByCreatedAtDesc(String roomId);

    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);

    boolean existsByUserIdAndRoomId(String userId, String roomId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.room.id = :roomId")
    Optional<Double> findAverageRatingByRoomId(@Param("roomId") String roomId);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double findOverallAverageRating();

    @Query("SELECT COUNT(r) FROM Review r WHERE r.room.id = :roomId AND r.rating = :rating")
    Long countByRoomIdAndRating(@Param("roomId") String roomId, @Param("rating") Integer rating);

    Optional<Review> findByUserIdAndRoomId(String userId, String roomId);

    boolean existsById(Integer id);

    long countByRoomId(String roomId);
}