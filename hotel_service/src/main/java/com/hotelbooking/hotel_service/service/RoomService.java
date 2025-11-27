package com.hotelbooking.hotel_service.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hotelbooking.hotel_service.client.UserServiceClient;
import com.hotelbooking.hotel_service.domain.ReviewWithUser;
import com.hotelbooking.hotel_service.domain.RoomResponseDTO;
import com.hotelbooking.hotel_service.domain.RoomType;
import com.hotelbooking.hotel_service.dto.ImageDto;
import com.hotelbooking.hotel_service.dto.RoomRequestDto;
import com.hotelbooking.hotel_service.dto.UserResponse;
import com.hotelbooking.hotel_service.model.Image;
import com.hotelbooking.hotel_service.model.Review;
import com.hotelbooking.hotel_service.model.Room;
import com.hotelbooking.hotel_service.repository.ImageRepository;
import com.hotelbooking.hotel_service.repository.RoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ImageRepository imageRepository;
    private final UserServiceClient userServiceClient;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public Page<Room> getAllRooms(
            String type,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer capacity,
            Boolean isAvailable,
            String search,
            Pageable pageable) {
        Specification<Room> spec = Specification.where(null);
        System.out.println("===== PARAMS =====");
        System.out.println("type = " + type);
        System.out.println("minPrice = " + minPrice);
        System.out.println("maxPrice = " + maxPrice);
        System.out.println("capacity = " + capacity);
        System.out.println("isAvailable = " + isAvailable);
        System.out.println("search = " + search);
        System.out.println("pageable = " + pageable);
        System.out.println("==================");

        if (type != null && !type.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(criteriaBuilder.lower(root.get("type")), type.toUpperCase()));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .greaterThanOrEqualTo(root.get("pricePerNight"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .lessThanOrEqualTo(root.get("pricePerNight"), maxPrice));
        }

        if (capacity != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("capacity"),
                    capacity));
        }

        if (isAvailable != null) {
            spec = spec
                    .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isAvailable"), isAvailable));
        }

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .like(criteriaBuilder.lower(root.get("roomNumber")), "%" + searchLower + "%"));
        }

        return roomRepository.findAll(spec, pageable);
    }

    public Optional<RoomResponseDTO> getRoomById(String id) {
        Optional<Room> roomOpt = roomRepository.findById(id);

        if (roomOpt.isEmpty()) {
            return Optional.empty();
        }

        Room room = roomOpt.get();
        RoomResponseDTO roomResponse = mapToRoomResponseDTO(room);

        if (room.getReviews() != null && !room.getReviews().isEmpty()) {
            List<ReviewWithUser> reviewsWithUsers = getReviewsWithUserInfo(room.getReviews());
            roomResponse.setReviews(reviewsWithUsers);
        }

        return Optional.of(roomResponse);
    }

    private List<ReviewWithUser> getReviewsWithUserInfo(List<Review> reviews) {
        List<String> userIds = reviews.stream()
                .map(Review::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<UserResponse> users = userServiceClient.getUsersByIds(userIds)
                .block();

        return reviews.stream()
                .map(review -> {
                    ReviewWithUser dto = new ReviewWithUser();
                    dto.setId(review.getId());
                    dto.setUserId(review.getUserId());
                    dto.setRating(review.getRating());
                    dto.setComment(review.getComment());
                    dto.setCreatedAt(review.getCreatedAt());

                    users.stream()
                            .filter(user -> user.getId().equals(review.getUserId()))
                            .findFirst()
                            .ifPresent(dto::setUser);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private RoomResponseDTO mapToRoomResponseDTO(Room room) {
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setImages(room.getImages() != null ? room.getImages().stream().map(image -> {
            ImageDto imageDTO = new ImageDto();
            imageDTO.setId(image.getId());
            imageDTO.setImage(image.getImage());
            return imageDTO;
        }).collect(Collectors.toList()) : null);
        dto.setType(room.getType());
        dto.setPricePerNight(room.getPricePerNight());
        dto.setDescription(room.getDescription());
        dto.setLocation(room.getLocation());
        dto.setCapacity(room.getCapacity());
        dto.setAmenities(room.getAmenities());
        dto.setIsAvailable(room.getIsAvailable());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());

        return dto;
    }

    public Room createRoom(RoomRequestDto dto, List<MultipartFile> images) throws IOException {
        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setType(RoomType.valueOf(dto.getType()));
        room.setPricePerNight(dto.getPricePerNight());
        room.setLocation(dto.getLocation());
        room.setCapacity(dto.getCapacity());
        room.setDescription(dto.getDescription());
        room.setIsAvailable(dto.getIsAvailable());

        if (dto.getAmenities() != null) {
            room.setAmenities(dto.getAmenities());
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        room = roomRepository.save(room);

        List<Image> imageList = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file.isEmpty()) {
                    continue;
                }

                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                file.transferTo(filePath.toFile());

                Image image = new Image();
                image.setName(file.getOriginalFilename());
                image.setImage(filename);
                image.setRoom(room);
                imageList.add(imageRepository.save(image));
            }
        }

        room.setImages(imageList);
        return roomRepository.save(room);
    }

    public Optional<Room> updateRoom(String id, RoomRequestDto roomDetails) {
        Optional<Room> roomOpt = roomRepository.findById(id);
        if (roomOpt.isEmpty()) {
            return Optional.empty();
        }

        Room room = roomOpt.get();
        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setType(RoomType.valueOf(roomDetails.getType()));
        room.setPricePerNight(roomDetails.getPricePerNight());
        room.setLocation(roomDetails.getLocation());
        room.setCapacity(roomDetails.getCapacity());
        room.setIsAvailable(roomDetails.getIsAvailable());
        room.setDescription(roomDetails.getDescription());

        if (roomDetails.getAmenities() != null) {
            room.setAmenities(roomDetails.getAmenities());
        }

        return Optional.of(roomRepository.save(room));
    }

    public boolean deleteRoom(String id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deleteImage(String roomId, String imageId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        Optional<Image> imageOpt = imageRepository.findById(imageId);

        if (roomOpt.isPresent() && imageOpt.isPresent()) {
            Room room = roomOpt.get();
            Image image = imageOpt.get();

            if (image.getRoom() != null && image.getRoom().getId().equals(roomId)) {
                try {
                    Path imagePath = Paths.get(uploadDir + image.getImage());
                    Files.deleteIfExists(imagePath);

                    room.getImages().removeIf(img -> img.getId().equals(imageId));

                    imageRepository.deleteById(imageId);

                    roomRepository.save(room);

                    return true;
                } catch (IOException e) {
                    System.err.println("Error deleting image file: " + e.getMessage());
                    room.getImages().removeIf(img -> img.getId().equals(imageId));
                    imageRepository.deleteById(imageId);
                    roomRepository.save(room);
                    return true;
                }
            }
        }
        return false;
    }

    public Room addImagesToRoom(String roomId, List<MultipartFile> images) throws IOException {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            throw new RuntimeException("Room not found with id: " + roomId);
        }

        Room room = roomOpt.get();
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file.isEmpty()) {
                    continue;
                }

                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                file.transferTo(filePath.toFile());

                Image image = new Image();
                image.setName(file.getOriginalFilename());
                image.setImage(filename);

                room.addImage(image);
            }

            room = roomRepository.save(room);
        }

        return room;
    }

    public List<Image> getRoomImages(String roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        return roomOpt.map(Room::getImages).orElse(new ArrayList<>());
    }

    public boolean deleteAllImages(String roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            List<Image> images = room.getImages();

            if (images != null && !images.isEmpty()) {
                for (Image image : images) {
                    try {
                        Path imagePath = Paths.get(uploadDir + image.getImage());
                        Files.deleteIfExists(imagePath);
                    } catch (IOException e) {
                        System.err.println("Error deleting image file: " + image.getImage() + " - " + e.getMessage());
                    }
                    imageRepository.deleteById(image.getId());
                }

                room.getImages().clear();
                roomRepository.save(room);
            }
            return true;
        }
        return false;
    }

    public boolean getIsAvailable(String roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            throw new RuntimeException("Room not found with id: " + roomId);
        }
                return room.getIsAvailable();
    }

}