package com.hotelbooking.hotel_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.hotel_service.dto.RoomRequestDto;
import com.hotelbooking.hotel_service.model.Room;
import com.hotelbooking.hotel_service.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
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
}
