package com.hotelbooking.hotel_service.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedReviewResponse {
    private List<ReviewResponseDto> reviews;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
    private boolean first;
    private boolean last;

    public static PaginatedReviewResponse fromPage(Page<ReviewResponseDto> page) {
        return PaginatedReviewResponse.builder()
                .reviews(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}