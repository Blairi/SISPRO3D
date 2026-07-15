package com.sispro3d.unam.review.mapper;

import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    /**
     * Maps scalar fields only. Relations (client, offeredService)
     * are resolved by the service layer via repository lookups.
     */
    public Review toEntity(ReviewRequest request) {
        if (request == null) {
            return null;
        }
        var review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return review;
    }

    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .clientId(review.getClient() != null ? review.getClient().getIdUser() : null)
                .offeredServiceId(review.getOfferedService() != null ? review.getOfferedService().getId() : null)
                .createdAt(review.getCreatedAt())
                .build();
    }

    public void updateEntityFromRequest(ReviewRequest request, Review review) {
        review.setRating(request.getRating());
        review.setComment(request.getComment());
    }
}
