package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.ReviewSummaryDTO;
import com.sispro3d.unam.api.dto.ServiceRequestDTO;
import com.sispro3d.unam.api.dto.ServiceResponseDTO;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.review.domain.Review;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ApiServiceMapper {

    /**
     * Maps scalar fields only. Relations (expert, category) are resolved and
     * assigned by the service layer, matching the existing project convention.
     */
    public OfferedService toEntity(ServiceRequestDTO request) {
        if (request == null) {
            return null;
        }
        OfferedService service = new OfferedService();
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setDeliveryTimeDays(request.getDeliveryTimeDays());
        return service;
    }

    public ServiceResponseDTO toResponse(OfferedService service, List<Review> reviews) {
        if (service == null) {
            return null;
        }
        List<ReviewSummaryDTO> summary = reviews == null
                ? Collections.emptyList()
                : reviews.stream().map(this::toSummary).toList();
        return ServiceResponseDTO.builder()
                .id(service.getId())
                .title(service.getTitle())
                .description(service.getDescription())
                .basePrice(service.getBasePrice())
                .expertId(service.getExpert() != null ? service.getExpert().getIdUser() : null)
                .expertName(service.getExpert() != null
                        ? service.getExpert().getName() + " " + service.getExpert().getLastName()
                        : null)
                .categoryId(service.getCategory() != null ? service.getCategory().getId() : null)
                .categoryName(service.getCategory() != null ? service.getCategory().getName() : null)
                .status(service.getStatus())
                .deliveryTimeDays(service.getDeliveryTimeDays())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .reviews(summary)
                .build();
    }

    public void updateEntityFromRequest(ServiceRequestDTO request, OfferedService service) {
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setDeliveryTimeDays(request.getDeliveryTimeDays());
    }

    private ReviewSummaryDTO toSummary(Review review) {
        return ReviewSummaryDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .clientId(review.getClient() != null ? review.getClient().getIdUser() : null)
                .clientName(review.getClient() != null
                        ? review.getClient().getName() + " " + review.getClient().getLastName()
                        : null)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
