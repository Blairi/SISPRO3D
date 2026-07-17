package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.review.mapper.ReviewMapper;
import com.sispro3d.unam.review.repository.ReviewRepository;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Override
    public List<ReviewResponse> findAll() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<ReviewResponse> findById(Long id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toResponse);
    }

    @Override
    public ReviewResponse create(ReviewRequest request) {
        if (!accountRepository.existsById(request.getClientId())) {
            throw ResourceNotFoundException.forId("Account (client)", request.getClientId());
        }
        if (!offeredServiceRepository.existsById(request.getOfferedServiceId())) {
            throw ResourceNotFoundException.forId("OfferedService", request.getOfferedServiceId());
        }

        Review review = reviewMapper.toEntity(request);
        review.setClient(accountRepository.getReferenceById(request.getClientId()));
        review.setOfferedService(offeredServiceRepository.getReferenceById(request.getOfferedServiceId()));
        review.setCreatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        return reviewMapper.toResponse(saved);
    }

    @Override
    public ReviewResponse update(Long id, ReviewRequest request) {
        Review existing = reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Review", id));

        if (!accountRepository.existsById(request.getClientId())) {
            throw ResourceNotFoundException.forId("Account (client)", request.getClientId());
        }
        if (!offeredServiceRepository.existsById(request.getOfferedServiceId())) {
            throw ResourceNotFoundException.forId("OfferedService", request.getOfferedServiceId());
        }

        reviewMapper.updateEntityFromRequest(request, existing);
        existing.setClient(accountRepository.getReferenceById(request.getClientId()));
        existing.setOfferedService(offeredServiceRepository.getReferenceById(request.getOfferedServiceId()));
        Review updated = reviewRepository.save(existing);
        return reviewMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Review", id);
        }
        reviewRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return reviewRepository.existsById(id);
    }

    @Override
    public List<ReviewResponse> findByClientId(Long clientId) {
        return reviewRepository.findByClient_IdUser(clientId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public List<ReviewResponse> findByOfferedServiceId(Long offeredServiceId) {
        return reviewRepository.findByOfferedService_Id(offeredServiceId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<ReviewResponse> findByClientIdAndOfferedServiceId(Long clientId, Long offeredServiceId) {
        return reviewRepository.findByClient_IdUserAndOfferedService_Id(clientId, offeredServiceId)
                .map(reviewMapper::toResponse);
    }
}
