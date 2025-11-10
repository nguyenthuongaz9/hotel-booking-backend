package com.hotelbooking.hotel_service.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hotelbooking.hotel_service.domain.RoomType;
import com.hotelbooking.hotel_service.dto.RoomRequestDto;
import com.hotelbooking.hotel_service.model.Image;
import com.hotelbooking.hotel_service.model.Room;
import com.hotelbooking.hotel_service.repository.ImageRepository;
import com.hotelbooking.hotel_service.repository.RoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ImageRepository imageRepository;

    private final String uploadDir = System.getProperty("user.dir") + "/src/main/resources/public/uploads/";

    public Page<Room> getAllRooms(
            String type,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer capacity,
            Boolean isAvailable,
            String search,
            Pageable pageable
    ) {
        Specification<Room> spec = Specification.where(null);

        if (type != null && !type.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder)
                    -> criteriaBuilder.equal(criteriaBuilder.lower(root.get("type")), type.toLowerCase()));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, criteriaBuilder)
                    -> criteriaBuilder.greaterThanOrEqualTo(root.get("pricePerNight"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, criteriaBuilder)
                    -> criteriaBuilder.lessThanOrEqualTo(root.get("pricePerNight"), maxPrice));
        }

        if (capacity != null) {
            spec = spec.and((root, query, criteriaBuilder)
                    -> criteriaBuilder.greaterThanOrEqualTo(root.get("capacity"), capacity));
        }

        if (isAvailable != null) {
            spec = spec.and((root, query, criteriaBuilder)
                    -> criteriaBuilder.equal(root.get("isAvailable"), isAvailable));
        }

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            spec = spec.and((root, query, criteriaBuilder)
                    -> criteriaBuilder.like(criteriaBuilder.lower(root.get("roomNumber")), "%" + searchLower + "%"));
        }

        return roomRepository.findAll(spec, pageable);
    }

    public Optional<Room> getRoomById(String id) {
        return roomRepository.findById(id);
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

    public Optional<Room> updateRoom(String id, Room roomDetails, List<MultipartFile> newImages) throws IOException {
        Optional<Room> roomOpt = roomRepository.findById(id);
        if (roomOpt.isEmpty()) {
            return Optional.empty();
        }

        Room room = roomOpt.get();
        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setType(roomDetails.getType());
        room.setPricePerNight(roomDetails.getPricePerNight());
        room.setLocation(roomDetails.getLocation());
        room.setCapacity(roomDetails.getCapacity());
        room.setIsAvailable(roomDetails.getIsAvailable());

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (newImages != null && !newImages.isEmpty()) {
            List<Image> imageList = room.getImages() != null ? room.getImages() : new ArrayList<>();

            for (MultipartFile file : newImages) {
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
            room.setImages(imageList);
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

}
