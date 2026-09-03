package com.sispro3d.unam.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lightweight view of a review, embedded in a {@link ServiceResponseDTO}
 * instead of exposing the full review entity. Demonstrates the 1:N
 * OfferedService -> Review relationship.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryDTO {

    private Long id;
    private Integer rating;
    private String comment;
    private Long clientId;
    private String clientName;
    private LocalDateTime createdAt;
}
