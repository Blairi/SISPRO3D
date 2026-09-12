package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.ServiceResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.mapper.ApiServiceMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.favorite.domain.FavoriteService;
import com.sispro3d.unam.favorite.repository.FavoriteServiceRepository;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.review.repository.ReviewRepository;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiFavoriteService {

    private final FavoriteServiceRepository favoriteServiceRepository;
    private final AccountRepository accountRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final ReviewRepository reviewRepository;
    private final ApiServiceMapper mapper;

    public ApiFavoriteService(FavoriteServiceRepository favoriteServiceRepository,
                              AccountRepository accountRepository,
                              OfferedServiceRepository offeredServiceRepository,
                              ReviewRepository reviewRepository,
                              ApiServiceMapper mapper) {
        this.favoriteServiceRepository = favoriteServiceRepository;
        this.accountRepository = accountRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.reviewRepository = reviewRepository;
        this.mapper = mapper;
    }

    public ServiceResponseDTO associate(Long clientId, Long serviceId) {
        accountRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", clientId));
        var service = offeredServiceRepository.findById(serviceId)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", serviceId));

        if (favoriteServiceRepository.existsByClient_IdUserAndService_Id(clientId, serviceId)) {
            throw new DataIntegrityException("Service with id " + serviceId
                    + " is already a favorite of client " + clientId);
        }

        FavoriteService favorite = new FavoriteService();
        favorite.setClient(accountRepository.getReferenceById(clientId));
        favorite.setService(service);
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteServiceRepository.save(favorite);

        return mapper.toResponse(service, reviewRepository.findByOfferedService_Id(serviceId));
    }

    public List<ServiceResponseDTO> findByClientId(Long clientId) {
        if (!accountRepository.existsById(clientId)) {
            throw ResourceNotFoundException.forId("Account (client)", clientId);
        }
        return favoriteServiceRepository.findByClient_IdUserOrderByCreatedAtDesc(clientId).stream()
                .map(favorite -> mapper.toResponse(favorite.getService(),
                        reviewRepository.findByOfferedService_Id(favorite.getService().getId())))
                .toList();
    }

    @Transactional
    public void remove(Long clientId, Long serviceId) {
        if (!favoriteServiceRepository.existsByClient_IdUserAndService_Id(clientId, serviceId)) {
            throw new ResourceNotFoundException("Favorite association not found for client "
                    + clientId + " and service " + serviceId);
        }
        favoriteServiceRepository.deleteByClient_IdUserAndService_Id(clientId, serviceId);
    }
}