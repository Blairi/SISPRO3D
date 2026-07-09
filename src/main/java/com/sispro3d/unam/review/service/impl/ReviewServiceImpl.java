package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.core.dto.ClientRef;
import com.sispro3d.unam.core.dto.OfferedServiceRef;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.dto.ReviewDTO;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Client;

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
            Client client = new Client();
            client.setAccount(new Account(dto.getClient().getId()));
            review.setClient(client);
        }

        if (dto.getOfferedService() != null) {
            review.setOfferedService(mapOfferedServiceByRef(dto.getOfferedService()));
        }

        return review;
    }

    private ReviewDTO toResponseDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getClient() != null && review.getClient().getAccount() != null) {
            ClientRef clientRef = ClientRef.builder()
                    .id(review.getClient().getAccount().getIdUser())
                    .name(review.getClient().getAccount().getName())
                    .lastName(review.getClient().getAccount().getLastName())
                    .email(review.getClient().getAccount().getEmail())
                    .build();
            dto.setClient(clientRef);
        }

        if (review.getOfferedService() != null) {
            dto.setOfferedService(mapOfferedServiceToRef(review.getOfferedService()));
        }

        return dto;
    }

    private OfferedService mapOfferedServiceByRef(OfferedServiceRef ref) {
        OfferedService offeredService = new OfferedService();
        offeredService.setId(ref.getId());
        offeredService.setTitle(ref.getTitle());
        offeredService.setDescription(ref.getDescription());
        offeredService.setBasePrice(ref.getBasePrice());
        offeredService.setDeliveryTimeDays(ref.getDeliveryTimeDays());
        return offeredService;
    }

    private OfferedServiceRef mapOfferedServiceToRef(OfferedService offeredService) {
        return OfferedServiceRef.builder()
                .id(offeredService.getId())
                .title(offeredService.getTitle())
                .description(offeredService.getDescription())
                .basePrice(offeredService.getBasePrice())
                .deliveryTimeDays(offeredService.getDeliveryTimeDays())
                .createdAt(offeredService.getCreatedAt())
                .updatedAt(offeredService.getUpdatedAt())
                .build();
    }
}
