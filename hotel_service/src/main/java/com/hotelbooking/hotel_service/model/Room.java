package com.hotelbooking.hotel_service.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hotelbooking.hotel_service.domain.Amenities;
import com.hotelbooking.hotel_service.domain.RoomType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "rooms")
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String roomNumber;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    @JsonManagedReference
    private List<Image> images;

    @Enumerated(EnumType.STRING)
    private RoomType type;

    private BigDecimal pricePerNight;
    private String description;
    private String location;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    private List<Amenities> amenities;

    private Boolean isAvailable;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
}
