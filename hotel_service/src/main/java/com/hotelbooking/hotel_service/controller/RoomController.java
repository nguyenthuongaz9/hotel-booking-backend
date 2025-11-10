package com.hotelbooking.hotel_service.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.hotel_service.dto.ReviewResponseDto;
import com.hotelbooking.hotel_service.dto.RoomRequestDto;
import com.hotelbooking.hotel_service.model.Room;
import com.hotelbooking.hotel_service.service.ReviewService;
import com.hotelbooking.hotel_service.service.RoomService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final ReviewService reviewService;

    @GetMapping
    public Page<Room> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Boolean isAvailable) {

        // Xử lý sort parameter
        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
                pageable = PageRequest.of(page, size, direction, sortParams[0]);
            } else {
                pageable = PageRequest.of(page, size);
            }
        } else {
            pageable = PageRequest.of(page, size);
        }

        return roomService.getAllRooms(type, minPrice, maxPrice, capacity, isAvailable, search, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable String id) {
        Optional<Room> room = roomService.getRoomById(id);
        return room.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Room> createRoom(
            @RequestPart("room") String roomJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        RoomRequestDto room = mapper.readValue(roomJson, RoomRequestDto.class);
        Room savedRoom = roomService.createRoom(room, images);
        return ResponseEntity.ok(savedRoom);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(
            @PathVariable String id,
            @RequestPart("room") Room roomDetails,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        Optional<Room> updatedRoom = roomService.updateRoom(id, roomDetails, images);
        return updatedRoom.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String id) {
        boolean deleted = roomService.deleteRoom(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/featured-reviews")
    public ResponseEntity<List<ReviewResponseDto>> getFeaturedReviews() {
        return reviewService.getTopReviewsForHomepage();
    }
    
}
