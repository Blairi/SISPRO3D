package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.ReviewRequestDTO;
import com.sispro3d.unam.api.dto.ReviewResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.mapper.ApiReviewMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.review.repository.ReviewRepository;
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

    public ApiReviewService(ReviewRepository reviewRepository,
                            AccountRepository accountRepository,
                            OfferedServiceRepository offeredServiceRepository,
                            ApiReviewMapper mapper) {
        this.reviewRepository = reviewRepository;
        this.accountRepository = accountRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.mapper = mapper;
    }

    public List<ReviewResponseDTO> findByServiceId(Long serviceId) {
        if (!offeredServiceRepository.existsById(serviceId)) {
            throw ResourceNotFoundException.forId("OfferedService", serviceId);
        }
        return reviewRepository.findByOfferedService_Id(serviceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public ReviewResponseDTO create(Long serviceId, ReviewRequestDTO request) {
        var service = offeredServiceRepository.findById(serviceId)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", serviceId));

        var client = accountRepository.findById(request.getClientId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", request.getClientId()));

        if (reviewRepository.findByClient_IdUserAndOfferedService_Id(request.getClientId(), serviceId).isPresent()) {
            throw new DataIntegrityException("Client with id " + request.getClientId()
                    + " has already reviewed service with id " + serviceId);
        }

        var review = mapper.toEntity(request);
        review.setClient(client);
        review.setOfferedService(service);
        review.setCreatedAt(LocalDateTime.now());

        var saved = reviewRepository.save(review);
        return mapper.toResponse(saved);
    }

    public ReviewResponseDTO update(Long id, ReviewRequestDTO request) {
        var existing = reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Review", id));

        var serviceId = existing.getOfferedService().getId();
        if (reviewRepository.findByClient_IdUserAndOfferedService_Id(request.getClientId(), serviceId)
                .filter(r -> !r.getId().equals(id))
                .isPresent()) {
            throw new DataIntegrityException("Client with id " + request.getClientId()
                    + " has already reviewed service with id " + serviceId);
        }

        var client = accountRepository.findById(request.getClientId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", request.getClientId()));

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
