package com.hotelbooking.hotel_service.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.hotel_service.domain.ReviewRequest;
import com.hotelbooking.hotel_service.domain.ReviewResponse;
import com.hotelbooking.hotel_service.domain.RoomResponseDTO;
import com.hotelbooking.hotel_service.dto.ReviewSummary;
import com.hotelbooking.hotel_service.dto.RoomRequestDto;
import com.hotelbooking.hotel_service.model.Image;
import com.hotelbooking.hotel_service.model.Room;
import com.hotelbooking.hotel_service.service.ReviewService;
import com.hotelbooking.hotel_service.service.RoomService;

import jakarta.validation.Valid;
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
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable String id) {
        Optional<RoomResponseDTO> room = roomService.getRoomById(id);

        return room.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Room> createRoom(
            @RequestPart("room") String roomJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        RoomRequestDto room = mapper.readValue(roomJson, RoomRequestDto.class);
        Room savedRoom = roomService.createRoom(room, images);
        return ResponseEntity.ok(savedRoom);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String id) {
        boolean deleted = roomService.deleteRoom(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{roomId}/images/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String roomId,
            @PathVariable String imageId) {
        try {
            boolean deleted = roomService.deleteImage(roomId, imageId);
            if (deleted) {
                return ResponseEntity.ok().body("Image deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Image or room not found, or image does not belong to the room");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting image: " + e.getMessage());
        }
    }

    @GetMapping("/{roomId}/isAvailable")
    public ResponseEntity<?> isAvailableRoom(@PathVariable String roomId) {
        try {
            boolean isAvailable = roomService.getIsAvailable(roomId);
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking room availability: " + e.getMessage());
        }
    }

    @PostMapping(value = "/{roomId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addImagesToRoom(
            @PathVariable String roomId,
            @RequestPart("images") List<MultipartFile> images) {
        try {
            if (images == null || images.isEmpty()) {
                return ResponseEntity.badRequest().body("No images provided");
            }

            for (MultipartFile file : images) {
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body("One or more files are empty");
                }
                if (!file.getContentType().startsWith("image/")) {
                    return ResponseEntity.badRequest().body("File " + file.getOriginalFilename() + " is not an image");
                }
            }

            Room updatedRoom = roomService.addImagesToRoom(roomId, images);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading images: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    @GetMapping("/{roomId}/images")
    public ResponseEntity<?> getRoomImages(@PathVariable String roomId) {
        try {
            List<Image> images = roomService.getRoomImages(roomId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving images: " + e.getMessage());
        }
    }

    @DeleteMapping("/{roomId}/images")
    public ResponseEntity<?> deleteAllImages(@PathVariable String roomId) {
        try {
            boolean deleted = roomService.deleteAllImages(roomId);
            if (deleted) {
                return ResponseEntity.ok().body("All images deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Room not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting images: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateRoom(
            @PathVariable String id,
            @RequestBody RoomRequestDto roomDetails) {
        try {

            Optional<Room> updatedRoom = roomService.updateRoom(id, roomDetails);

            if (updatedRoom.isPresent()) {
                return ResponseEntity.ok(updatedRoom.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Room not found with id: " + id);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating room: " + e.getMessage());
        }
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        try {
            System.out.println("Received review request: " + reviewRequest);
            System.out.println("User ID: " + reviewRequest.getUserId());
            System.out.println("Room ID: " + reviewRequest.getRoomId());
            System.out.println("Rating: " + reviewRequest.getRating());
            System.out.println("Comment: " + reviewRequest.getComment());

            ReviewResponse response = reviewService.createReview(reviewRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("Error creating review: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reviews/{roomId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByRoomId(@PathVariable String roomId) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByRoomId(roomId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reviews/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(@PathVariable String userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/{roomId}/summary")
    public ResponseEntity<ReviewSummary> getReviewSummary(@PathVariable String roomId) {
        try {
            ReviewSummary summary = reviewService.getReviewSummary(roomId);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reviews/check")
    public ResponseEntity<Boolean> hasUserReviewedRoom(
            @RequestParam String userId,
            @RequestParam String roomId) {
        boolean hasReviewed = reviewService.hasUserReviewedRoom(userId, roomId);
        return ResponseEntity.ok(hasReviewed);
    }

    @GetMapping("/reviews/user-room")
    public ResponseEntity<ReviewResponse> getReviewByUserAndRoom(
            @RequestParam String userId,
            @RequestParam String roomId) {
        ReviewResponse review = reviewService.getReviewByUserAndRoom(userId, roomId);
        if (review != null) {
            return ResponseEntity.ok(review);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Integer reviewId,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        try {
            ReviewResponse response = reviewService.updateReview(reviewId, reviewRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reviews/overall-average-rating")
    public ResponseEntity<?> getOverallRating() {
        return reviewService.getOverallAverageRating();
    }

}
