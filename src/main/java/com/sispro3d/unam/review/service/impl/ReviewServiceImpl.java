package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.OfferedServiceRef;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.review.repository.ReviewRepository;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.user.domain.Account;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<ReviewResponse> findAll() {
        return reviewRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<ReviewResponse> findById(Long id) {
        return reviewRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public ReviewResponse create(ReviewRequest request) {
        Review review = toEntity(request);
        Review saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    @Override
    public ReviewResponse update(Long id, ReviewRequest request) {
        int pk = id.intValue();
        reviewRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada con id: " + id));

        Review review = toEntity(request);
        review.setId(pk);
        Review updated = reviewRepository.update(review);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        reviewRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada con id: " + id));
        reviewRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return reviewRepository.existsById(id.intValue());
    }

    private Review toEntity(ReviewRequest request) {
        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setClient(new Account(request.getClientId()));
        review.setOfferedService(new OfferedService(request.getOfferedServiceId()));
        return review;
    }

    private ReviewResponse toResponse(Review review) {
        ReviewResponse.ReviewResponseBuilder builder = ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt());

        if (review.getClient() != null) {
            builder.client(AccountRef.builder()
                    .idUser(review.getClient().getIdUser())
                    .name(review.getClient().getName())
                    .lastName(review.getClient().getLastName())
                    .email(review.getClient().getEmail())
                    .build());
        }

        if (review.getOfferedService() != null) {
            builder.offeredService(OfferedServiceRef.builder()
                    .id(review.getOfferedService().getId())
                    .title(review.getOfferedService().getTitle())
                    .description(review.getOfferedService().getDescription())
                    .basePrice(review.getOfferedService().getBasePrice())
                    .deliveryTimeDays(review.getOfferedService().getDeliveryTimeDays())
                    .createdAt(review.getOfferedService().getCreatedAt())
                    .updatedAt(review.getOfferedService().getUpdatedAt())
                    .build());
        }

        return builder.build();
    }
}
