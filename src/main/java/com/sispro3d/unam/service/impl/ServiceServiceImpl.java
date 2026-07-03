package com.sispro3d.unam.service.impl;

import com.sispro3d.unam.dao.GenericDAO;
import com.sispro3d.unam.domain.*;
import com.sispro3d.unam.dto.*;
import com.sispro3d.unam.service.ServiceService;

import java.util.List;
import java.util.Optional;

public class ServiceServiceImpl implements ServiceService {

    private final GenericDAO<Service> serviceDAO;

    public ServiceServiceImpl(GenericDAO<Service> serviceDAO) {
        this.serviceDAO = serviceDAO;
    }

    @Override
    public List<ServiceDTO> findAll() {
        return serviceDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<ServiceDTO> findById(int id) {
        return serviceDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public ServiceDTO create(ServiceDTO dto) {
        Service service = toEntity(dto);
        int generatedId = serviceDAO.insert(service);
        service.setId(generatedId);
        return toResponseDTO(service);
    }

    @Override
    public ServiceDTO update(int id, ServiceDTO dto) {
        serviceDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + id));

        Service service = toEntity(dto);
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

    private Service toEntity(ServiceDTO dto) {
        Service service = new Service();
        service.setTitle(dto.getTitle());
        service.setDescription(dto.getDescription());
        service.setBasePrice(dto.getBasePrice());
        service.setDeliveryTimeDays(dto.getDeliveryTimeDays());

        if (dto.getAdmin() != null && dto.getAdmin().getAccount() != null) {
            Admin admin = new Admin();
            admin.setAccount(new Account(dto.getAdmin().getAccount().getIdUser()));
            service.setAdmin(admin);
        }

        if (dto.getExpert() != null && dto.getExpert().getAccount() != null) {
            Expert expert = new Expert();
            expert.setAccount(new Account(dto.getExpert().getAccount().getIdUser()));
            expert.setSpecialty(dto.getExpert().getSpecialty());
            expert.setPortfolioUrl(dto.getExpert().getPortfolioUrl());
            expert.setBio(dto.getExpert().getBio());
            expert.setYearsExperience(dto.getExpert().getYearsExperience());
            service.setExpert(expert);
        }

        if (dto.getCategory() != null) {
            Category category = new Category(dto.getCategory().getId());
            category.setName(dto.getCategory().getName());
            category.setDescription(dto.getCategory().getDescription());
            service.setCategory(category);
        }

        return service;
    }

    private ServiceDTO toResponseDTO(Service service) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setTitle(service.getTitle());
        dto.setDescription(service.getDescription());
        dto.setBasePrice(service.getBasePrice());
        dto.setDeliveryTimeDays(service.getDeliveryTimeDays());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());

        if (service.getAdmin() != null) {
            AdminDTO adminDTO = new AdminDTO();
            if (service.getAdmin().getAccount() != null) {
                AccountDTO accountDTO = mapAccountToDTO(service.getAdmin().getAccount());
                adminDTO.setAccount(accountDTO);
            }
            dto.setAdmin(adminDTO);
        }

        if (service.getExpert() != null) {
            ExpertDTO expertDTO = new ExpertDTO();
            if (service.getExpert().getAccount() != null) {
                AccountDTO accountDTO = mapAccountToDTO(service.getExpert().getAccount());
                expertDTO.setAccount(accountDTO);
            }
            expertDTO.setSpecialty(service.getExpert().getSpecialty());
            expertDTO.setPortfolioUrl(service.getExpert().getPortfolioUrl());
            expertDTO.setBio(service.getExpert().getBio());
            expertDTO.setYearsExperience(service.getExpert().getYearsExperience());
            dto.setExpert(expertDTO);
        }

        if (service.getCategory() != null) {
            CategoryDTO categoryDTO = new CategoryDTO(service.getCategory().getId(),
                    service.getCategory().getName(),
                    service.getCategory().getDescription());
            dto.setCategory(categoryDTO);
        }

        return dto;
    }

    private AccountDTO mapAccountToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setIdUser(account.getIdUser());
        dto.setName(account.getName());
        dto.setLastName(account.getLastName());
        dto.setEmail(account.getEmail());
        dto.setPhone(account.getPhone());
        dto.setPassword(account.getPassword());
        dto.setType(account.getType());
        dto.setCreatedAt(account.getCreatedAt());
        return dto;
    }
}
