package com.sispro3d.unam.user.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Expert;
import com.sispro3d.unam.user.dto.AccountDTO;
import com.sispro3d.unam.user.dto.ExpertDTO;
import com.sispro3d.unam.user.service.ExpertService;

import java.util.List;
import java.util.Optional;

public class ExpertServiceImpl implements ExpertService {

    private final GenericDAO<Expert> expertDAO;

    public ExpertServiceImpl(GenericDAO<Expert> expertDAO) {
        this.expertDAO = expertDAO;
    }

    @Override
    public List<ExpertDTO> findAll() {
        return expertDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<ExpertDTO> findById(int id) {
        return expertDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public ExpertDTO create(ExpertDTO dto) {
        Expert expert = toEntity(dto);
        int generatedId = expertDAO.insert(expert);
        expert.getAccount().setIdUser(generatedId);
        return toResponseDTO(expert);
    }

    @Override
    public ExpertDTO update(int id, ExpertDTO dto) {
        expertDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Experto no encontrado con id: " + id));

        Expert expert = toEntity(dto);
        expert.getAccount().setIdUser(id);
        expertDAO.update(expert);
        return toResponseDTO(expert);
    }

    @Override
    public void delete(int id) {
        expertDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Experto no encontrado con id: " + id));
        expertDAO.delete(id);
    }

    private Expert toEntity(ExpertDTO dto) {
        Expert expert = new Expert();
        if (dto.getAccount() != null) {
            expert.setAccount(new Account(dto.getAccount().getIdUser()));
        }
        expert.setSpecialty(dto.getSpecialty());
        expert.setPortfolioUrl(dto.getPortfolioUrl());
        expert.setBio(dto.getBio());
        expert.setYearsExperience(dto.getYearsExperience());
        return expert;
    }

    private ExpertDTO toResponseDTO(Expert expert) {
        ExpertDTO dto = new ExpertDTO();
        if (expert.getAccount() != null) {
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setIdUser(expert.getAccount().getIdUser());
            accountDTO.setName(expert.getAccount().getName());
            accountDTO.setLastName(expert.getAccount().getLastName());
            accountDTO.setEmail(expert.getAccount().getEmail());
            accountDTO.setPhone(expert.getAccount().getPhone());
            accountDTO.setPassword(expert.getAccount().getPassword());
            accountDTO.setType(expert.getAccount().getType());
            accountDTO.setCreatedAt(expert.getAccount().getCreatedAt());
            dto.setAccount(accountDTO);
        }
        dto.setSpecialty(expert.getSpecialty());
        dto.setPortfolioUrl(expert.getPortfolioUrl());
        dto.setBio(expert.getBio());
        dto.setYearsExperience(expert.getYearsExperience());
        return dto;
    }
}
