package com.hotelbooking.hotel_service.repository;

import com.hotelbooking.hotel_service.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, String> {
}
