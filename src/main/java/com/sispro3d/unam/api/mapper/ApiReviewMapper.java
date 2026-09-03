package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.ReviewRequestDTO;
import com.sispro3d.unam.api.dto.ReviewResponseDTO;
import com.sispro3d.unam.review.domain.Review;
import org.springframework.stereotype.Component;

@Component
public class ApiReviewMapper {

    public Review toEntity(ReviewRequestDTO request) {
        if (request == null) {
            return null;
        }
        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return review;
    }

    public ReviewResponseDTO toResponse(Review entity) {
        if (entity == null) {
            return null;
        }
        return ReviewResponseDTO.builder()
                .id(entity.getId())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .clientId(entity.getClient() != null ? entity.getClient().getIdUser() : null)
                .clientName(entity.getClient() != null
                        ? entity.getClient().getName() + " " + entity.getClient().getLastName()
                        : null)
                .offeredServiceId(entity.getOfferedService() != null ? entity.getOfferedService().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public void updateEntityFromRequest(ReviewRequestDTO request, Review entity) {
        entity.setRating(request.getRating());
        entity.setComment(request.getComment());
    }
}
