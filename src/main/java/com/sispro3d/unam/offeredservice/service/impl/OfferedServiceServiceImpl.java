package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.CategoryRef;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.user.domain.Account;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OfferedServiceServiceImpl implements OfferedServiceService {

    private final OfferedServiceRepository offeredServiceRepository;

    public OfferedServiceServiceImpl(OfferedServiceRepository offeredServiceRepository) {
        this.offeredServiceRepository = offeredServiceRepository;
    }

    @Override
    public List<OfferedServiceResponse> findAll() {
        return offeredServiceRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<OfferedServiceResponse> findById(Long id) {
        return offeredServiceRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public OfferedServiceResponse create(OfferedServiceRequest request) {
        OfferedService service = toEntity(request);
        OfferedService saved = offeredServiceRepository.save(service);
        return toResponse(saved);
    }

    @Override
    public OfferedServiceResponse update(Long id, OfferedServiceRequest request) {
        int pk = id.intValue();
        offeredServiceRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + id));

        OfferedService service = toEntity(request);
        service.setId(pk);
        OfferedService updated = offeredServiceRepository.update(service);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        offeredServiceRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + id));
        offeredServiceRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return offeredServiceRepository.existsById(id.intValue());
    }

    private OfferedService toEntity(OfferedServiceRequest request) {
        OfferedService service = new OfferedService();
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setDeliveryTimeDays(request.getDeliveryTimeDays());

        if (request.getAdminId() != null) {
            service.setAdmin(new Account(request.getAdminId()));
        }

        service.setExpert(new Account(request.getExpertId()));

        Category category = new Category();
        category.setId(request.getCategoryId());
        service.setCategory(category);

        return service;
    }

    private OfferedServiceResponse toResponse(OfferedService service) {
        OfferedServiceResponse.OfferedServiceResponseBuilder builder = OfferedServiceResponse.builder()
                .id(service.getId())
                .title(service.getTitle())
                .description(service.getDescription())
                .basePrice(service.getBasePrice())
                .deliveryTimeDays(service.getDeliveryTimeDays())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt());

        if (service.getAdmin() != null) {
            builder.admin(AccountRef.builder()
                    .idUser(service.getAdmin().getIdUser())
                    .name(service.getAdmin().getName())
                    .lastName(service.getAdmin().getLastName())
                    .email(service.getAdmin().getEmail())
                    .build());
        }

        if (service.getExpert() != null) {
            builder.expert(AccountRef.builder()
                    .idUser(service.getExpert().getIdUser())
                    .name(service.getExpert().getName())
                    .lastName(service.getExpert().getLastName())
                    .email(service.getExpert().getEmail())
                    .specialty(service.getExpert().getSpecialty())
                    .portfolioUrl(service.getExpert().getPortfolioUrl())
                    .bio(service.getExpert().getBio())
                    .yearsExperience(service.getExpert().getYearsExperience())
                    .build());
        }

        if (service.getCategory() != null) {
            builder.category(CategoryRef.builder()
                    .id(service.getCategory().getId())
                    .name(service.getCategory().getName())
                    .description(service.getCategory().getDescription())
                    .build());
        }

        return builder.build();
    }
}
