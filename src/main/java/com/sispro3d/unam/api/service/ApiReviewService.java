package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.ReviewRequestDTO;
import com.sispro3d.unam.api.dto.ReviewResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.mapper.ApiReviewMapper;
import com.sispro3d.unam.core.exception.ConflictException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.security.ApiCurrentUser;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.repository.ReviewRepository;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiReviewService {

    private final ReviewRepository reviewRepository;
    private final AccountRepository accountRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final ApiReviewMapper mapper;
    private final ReviewService reviewService;

    public ApiReviewService(ReviewRepository reviewRepository,
                            AccountRepository accountRepository,
                            OfferedServiceRepository offeredServiceRepository,
                            ApiReviewMapper mapper,
                            ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.accountRepository = accountRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.mapper = mapper;
        this.reviewService = reviewService;
    }

    private Review findReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Review", id));
    }

    public List<ReviewResponseDTO> findByServiceId(Long serviceId) {
        if (!offeredServiceRepository.existsById(serviceId)) {
            throw ResourceNotFoundException.forId("OfferedService", serviceId);
        }
        return reviewRepository.findByOfferedService_Id(serviceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Delega en el dominio, que exige rol CLIENT, una orden COMPLETED sobre el
     * servicio, ausencia de una reseña previa y un rating válido.
     */
    public ReviewResponseDTO create(Long serviceId, ReviewRequestDTO request) {
        Long clientId = ApiCurrentUser.requireAccountId();

        var created = reviewService.create(ReviewRequest.builder()
                .clientId(clientId)
                .offeredServiceId(serviceId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build());

        return mapper.toResponse(findReview(created.getId()));
    }

    public ReviewResponseDTO update(Long id, ReviewRequestDTO request) {
        var existing = reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Review", id));

        var serviceId = existing.getOfferedService().getId();
        Long clientId = ApiCurrentUser.requireAccountId();
        if (reviewRepository.findByClient_IdUserAndOfferedService_Id(clientId, serviceId)
                .filter(r -> !r.getId().equals(id))
                .isPresent()) {
            throw new ConflictException("Client with id " + clientId
                    + " has already reviewed service with id " + serviceId);
        }

        var client = accountRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", clientId));

        mapper.updateEntityFromRequest(request, existing);
        existing.setClient(client);

        return mapper.toResponse(reviewRepository.save(existing));
    }

    public void delete(Long id) {
        var existing = reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Review", id));
        reviewRepository.delete(existing);
    }
}
