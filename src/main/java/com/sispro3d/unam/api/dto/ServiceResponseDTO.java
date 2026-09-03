package com.sispro3d.unam.api.dto;

import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponseDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private Long expertId;
    private String expertName;
    private Long categoryId;
    private String categoryName;
    private ServiceStatus status;
    private Integer deliveryTimeDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Nested (summarized) reviews for this service — used to demonstrate the
     * OfferedService (1) -> Review (N) relationship.
     */
    private List<ReviewSummaryDTO> reviews;
}
