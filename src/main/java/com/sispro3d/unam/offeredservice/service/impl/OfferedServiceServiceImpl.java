package com.sispro3d.unam.offeredservice.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.dto.CategoryDTO;
import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceDTO;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Admin;
import com.sispro3d.unam.user.domain.Expert;
import com.sispro3d.unam.user.dto.AccountDTO;
import com.sispro3d.unam.user.dto.AdminDTO;
import com.sispro3d.unam.user.dto.ExpertDTO;

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

    private OfferedServiceDTO toResponseDTO(OfferedService service) {
        OfferedServiceDTO dto = new OfferedServiceDTO();
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
