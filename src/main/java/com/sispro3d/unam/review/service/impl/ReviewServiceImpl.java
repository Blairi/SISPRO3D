package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ConflictException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.review.mapper.ReviewMapper;
import com.sispro3d.unam.review.repository.ReviewRepository;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private OfferedServiceRepository offeredServiceRepository;
    @Autowired
    private WorkOrderRepository workOrderRepository;

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
        var client = accountRepository.findById(request.getClientId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", request.getClientId()));

        if (client.getRole() != Role.CLIENT) {
            log.warn("Intento de publicar reseña por cuenta no cliente: id={}", request.getClientId());
            throw new ForbiddenException("Solo cuentas de tipo CLIENT pueden publicar reseñas");
        }

        var service = offeredServiceRepository.findById(request.getOfferedServiceId())
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", request.getOfferedServiceId()));

        requireCompletedOrder(request.getClientId(), request.getOfferedServiceId());
        requireSinReseñaPrevia(request.getClientId(), request.getOfferedServiceId());
        requireRatingValido(request.getRating());

        Review review = reviewMapper.toEntity(request);
        review.setClient(client);
        review.setOfferedService(service);
        review.setCreatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        log.info("Reseña creada: id={}, cliente={}, servicio={}", saved.getId(), request.getClientId(), request.getOfferedServiceId());
        return reviewMapper.toResponse(saved);
    }

    private void requireCompletedOrder(Long clientId, Long offeredServiceId) {
        var completed = workOrderRepository
                .findByQuote_Client_IdUserAndQuote_OfferedService_IdAndStatus(
                        clientId, offeredServiceId, WorkOrderStatus.COMPLETED);

        if (completed.isEmpty()) {
            log.warn("Intento de reseñar sin orden completada: cliente={}, servicio={}", clientId, offeredServiceId);
            throw new BadRequestException(
                    "Solo se puede reseñar un servicio con una orden de trabajo completada");
        }
    }

    private void requireSinReseñaPrevia(Long clientId, Long offeredServiceId) {
        if (reviewRepository.findByClient_IdUserAndOfferedService_Id(clientId, offeredServiceId).isPresent()) {
            log.warn("Intento de reseñar servicio ya reseñado: cliente={}, servicio={}", clientId, offeredServiceId);
            throw new ConflictException("El cliente ya publicó una reseña para este servicio");
        }
    }

    private void requireRatingValido(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            log.warn("Calificación inválida al crear reseña: valor={}", rating);
            throw new BadRequestException("La calificación debe estar entre 1 y 5");
        }
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
        log.info("Reseña actualizada: id={}", updated.getId());
        return reviewMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Review", id);
        }
        reviewRepository.deleteById(id);
        log.info("Reseña eliminada: id={}", id);
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
