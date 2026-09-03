package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.ServiceRequestDTO;
import com.sispro3d.unam.api.dto.ServiceResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.mapper.ApiServiceMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.review.repository.ReviewRepository;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiServiceService {

    private final OfferedServiceRepository offeredServiceRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final QuoteRepository quoteRepository;
    private final ApiServiceMapper mapper;

    public ApiServiceService(OfferedServiceRepository offeredServiceRepository,
                             AccountRepository accountRepository,
                             CategoryRepository categoryRepository,
                             ReviewRepository reviewRepository,
                             QuoteRepository quoteRepository,
                             ApiServiceMapper mapper) {
        this.offeredServiceRepository = offeredServiceRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.quoteRepository = quoteRepository;
        this.mapper = mapper;
    }

    public List<ServiceResponseDTO> findAll(Long categoryId, Long expertId) {
        List<OfferedService> services;
        if (categoryId != null && expertId != null) {
            services = offeredServiceRepository.findAll().stream()
                    .filter(s -> s.getExpert() != null
                            && s.getExpert().getIdUser().equals(expertId)
                            && s.getCategory() != null
                            && s.getCategory().getId().equals(categoryId))
                    .toList();
        } else if (categoryId != null) {
            services = offeredServiceRepository.findByCategory_Id(categoryId);
        } else if (expertId != null) {
            services = offeredServiceRepository.findByExpert_IdUser(expertId);
        } else {
            services = offeredServiceRepository.findAll();
        }
        return services.stream()
                .map(s -> mapper.toResponse(s, reviewRepository.findByOfferedService_Id(s.getId())))
                .toList();
    }

    public ServiceResponseDTO findById(Long id) {
        var service = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));
        return mapper.toResponse(service, reviewRepository.findByOfferedService_Id(id));
    }

    public ServiceResponseDTO create(ServiceRequestDTO request) {
        var expert = accountRepository.findById(request.getExpertId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", request.getExpertId()));
        if (expert.getRole() != Role.EXPERT) {
            throw new IllegalArgumentException("Only EXPERT accounts can publish services");
        }
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw ResourceNotFoundException.forId("Category", request.getCategoryId());
        }

        var offeredService = mapper.toEntity(request);
        offeredService.setExpert(expert);
        offeredService.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        offeredService.setStatus(ServiceStatus.PENDING);
        offeredService.setCreatedAt(LocalDateTime.now());

        var saved = offeredServiceRepository.save(offeredService);
        return mapper.toResponse(saved, List.of());
    }

    public ServiceResponseDTO update(Long id, ServiceRequestDTO request) {
        var existing = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));

        if (!accountRepository.existsById(request.getExpertId())) {
            throw ResourceNotFoundException.forId("Account (expert)", request.getExpertId());
        }
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw ResourceNotFoundException.forId("Category", request.getCategoryId());
        }

        mapper.updateEntityFromRequest(request, existing);
        existing.setExpert(accountRepository.getReferenceById(request.getExpertId()));
        existing.setCategory(categoryRepository.getReferenceById(request.getCategoryId()));
        existing.setUpdatedAt(LocalDateTime.now());

        var updated = offeredServiceRepository.save(existing);
        return mapper.toResponse(updated, reviewRepository.findByOfferedService_Id(id));
    }

    @Transactional
    public void delete(Long id) {
        var existing = offeredServiceRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", id));
        if (!reviewRepository.findByOfferedService_Id(id).isEmpty()) {
            throw new DataIntegrityException("OfferedService with id " + id
                    + " has associated reviews and cannot be deleted");
        }
        if (!quoteRepository.findByOfferedService_Id(id).isEmpty()) {
            throw new DataIntegrityException("OfferedService with id " + id
                    + " has associated quotes and cannot be deleted");
        }
        offeredServiceRepository.delete(existing);
    }
}
