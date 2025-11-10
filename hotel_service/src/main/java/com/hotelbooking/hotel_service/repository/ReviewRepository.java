package com.hotelbooking.hotel_service.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hotelbooking.hotel_service.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByRoomId(String roomId);
    Page<Review> findByRoomId(String roomId, Pageable pageable);
    
    @Query("SELECT r FROM Review r ORDER BY r.rating DESC, r.createdAt DESC")
    Page<Review> findTopByOrderByRatingDesc(Pageable pageable);
    
    Page<Review> findAllByOrderByRatingDescCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT r FROM Review r ORDER BY r.rating DESC, r.createdAt DESC")
    List<Review> findTopNByOrderByRatingDesc(Pageable pageable);
}