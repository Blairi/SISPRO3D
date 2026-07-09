package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.core.dto.AdminRef;
import com.sispro3d.unam.core.dto.CategoryRef;
import com.sispro3d.unam.core.dto.ExpertRef;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceDTO;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Admin;
import com.sispro3d.unam.user.domain.Expert;

import java.util.List;
import java.util.Optional;

public class OfferedServiceServiceImpl implements OfferedServiceService {

    private final GenericDAO<OfferedService> serviceDAO;

    public OfferedServiceServiceImpl(GenericDAO<OfferedService> serviceDAO) {
        this.serviceDAO = serviceDAO;
    }

    @Override
    public List<OfferedServiceDTO> findAll() {
        return serviceDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<OfferedServiceDTO> findById(int id) {
        return serviceDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public OfferedServiceDTO create(OfferedServiceDTO dto) {
        OfferedService service = toEntity(dto);
        int generatedId = serviceDAO.insert(service);
        service.setId(generatedId);
        return toResponseDTO(service);
    }

    @Override
    public OfferedServiceDTO update(int id, OfferedServiceDTO dto) {
        serviceDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + id));

        OfferedService service = toEntity(dto);
        service.setId(id);
        serviceDAO.update(service);
        return toResponseDTO(service);
    }

    @Override
    public void delete(int id) {
        serviceDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + id));
        serviceDAO.delete(id);
    }

    private OfferedService toEntity(OfferedServiceDTO dto) {
        OfferedService service = new OfferedService();
        service.setTitle(dto.getTitle());
        service.setDescription(dto.getDescription());
        service.setBasePrice(dto.getBasePrice());
        service.setDeliveryTimeDays(dto.getDeliveryTimeDays());

        if (dto.getAdmin() != null) {
            Admin admin = new Admin();
            admin.setAccount(new Account(dto.getAdmin().getId()));
            service.setAdmin(admin);
        }

        if (dto.getExpert() != null) {
            Expert expert = new Expert();
            expert.setAccount(new Account(dto.getExpert().getId()));
            expert.setSpecialty(dto.getExpert().getSpecialty());
            expert.setPortfolioUrl(dto.getExpert().getPortfolioUrl());
            expert.setBio(dto.getExpert().getBio());
            expert.setYearsExperience(dto.getExpert().getYearsExperience());
            service.setExpert(expert);
        }

        if (dto.getCategory() != null) {
            Category category = new Category();
            category.setId(dto.getCategory().getId());
            category.setName(dto.getCategory().getName());
            category.setDescription(dto.getCategory().getDescription());
            service.setCategory(category);
        }

        return service;
    }

    private OfferedServiceDTO toResponseDTO(OfferedService service) {
        OfferedServiceDTO dto = new OfferedServiceDTO();
        dto.setId(service.getId());
        dto.setTitle(service.getTitle());
        dto.setDescription(service.getDescription());
        dto.setBasePrice(service.getBasePrice());
        dto.setDeliveryTimeDays(service.getDeliveryTimeDays());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());

        if (service.getAdmin() != null && service.getAdmin().getAccount() != null) {
            AdminRef adminRef = AdminRef.builder()
                    .id(service.getAdmin().getAccount().getIdUser())
                    .name(service.getAdmin().getAccount().getName())
                    .lastName(service.getAdmin().getAccount().getLastName())
                    .email(service.getAdmin().getAccount().getEmail())
                    .build();
            dto.setAdmin(adminRef);
        }

        if (service.getExpert() != null && service.getExpert().getAccount() != null) {
            ExpertRef expertRef = ExpertRef.builder()
                    .id(service.getExpert().getAccount().getIdUser())
                    .name(service.getExpert().getAccount().getName())
                    .lastName(service.getExpert().getAccount().getLastName())
                    .email(service.getExpert().getAccount().getEmail())
                    .specialty(service.getExpert().getSpecialty())
                    .portfolioUrl(service.getExpert().getPortfolioUrl())
                    .bio(service.getExpert().getBio())
                    .yearsExperience(service.getExpert().getYearsExperience())
                    .build();
            dto.setExpert(expertRef);
        }

        if (service.getCategory() != null) {
            CategoryRef categoryRef = CategoryRef.builder()
                    .id(service.getCategory().getId())
                    .name(service.getCategory().getName())
                    .description(service.getCategory().getDescription())
                    .build();
            dto.setCategory(categoryRef);
        }

        return dto;
    }


}
