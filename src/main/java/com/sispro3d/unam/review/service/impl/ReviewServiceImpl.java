package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.OfferedServiceRef;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.dto.ReviewDTO;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.user.domain.Account;

import java.util.List;
import java.util.Optional;

public class ReviewServiceImpl implements ReviewService {

    private final GenericDAO<Review> reviewDAO;

    public ReviewServiceImpl(GenericDAO<Review> reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    @Override
    public List<ReviewDTO> findAll() {
        return reviewDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<ReviewDTO> findById(int id) {
        return reviewDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public ReviewDTO create(ReviewDTO dto) {
        Review review = toEntity(dto);
        int generatedId = reviewDAO.insert(review);
        review.setId(generatedId);
        return toResponseDTO(review);
    }

    @Override
    public ReviewDTO update(int id, ReviewDTO dto) {
        reviewDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada con id: " + id));

        Review review = toEntity(dto);
        review.setId(id);
        reviewDAO.update(review);
        return toResponseDTO(review);
    }

    @Override
    public void delete(int id) {
        reviewDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada con id: " + id));
        reviewDAO.delete(id);
    }

    private Review toEntity(ReviewDTO dto) {
        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        if (dto.getClient() != null) {
            review.setClient(new Account(dto.getClient().getIdUser()));
        }

        if (dto.getOfferedService() != null) {
            review.setOfferedService(new OfferedService(dto.getOfferedService().getId()));
        }

        return review;
    }

    private ReviewDTO toResponseDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getClient() != null) {
            dto.setClient(AccountRef.builder()
                    .idUser(review.getClient().getIdUser())
                    .name(review.getClient().getName())
                    .lastName(review.getClient().getLastName())
                    .email(review.getClient().getEmail())
                    .build());
        }

        if (review.getOfferedService() != null) {
            dto.setOfferedService(OfferedServiceRef.builder()
                    .id(review.getOfferedService().getId())
                    .title(review.getOfferedService().getTitle())
                    .description(review.getOfferedService().getDescription())
                    .basePrice(review.getOfferedService().getBasePrice())
                    .deliveryTimeDays(review.getOfferedService().getDeliveryTimeDays())
                    .createdAt(review.getOfferedService().getCreatedAt())
                    .updatedAt(review.getOfferedService().getUpdatedAt())
                    .build());
        }

        return dto;
    }
}
