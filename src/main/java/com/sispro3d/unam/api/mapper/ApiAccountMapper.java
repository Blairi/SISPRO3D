package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.AccountRequestDTO;
import com.sispro3d.unam.api.dto.AccountResponseDTO;
import com.sispro3d.unam.user.domain.Account;
import org.springframework.stereotype.Component;

@Component
public class ApiAccountMapper {

    public Account toEntity(AccountRequestDTO request) {
        if (request == null) {
            return null;
        }
        Account account = new Account();
        account.setName(request.getName());
        account.setLastName(request.getLastName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setPassword(request.getPassword());
        account.setRole(request.getRole());
        account.setSpecialty(request.getSpecialty());
        account.setPortfolioUrl(request.getPortfolioUrl());
        account.setBio(request.getBio());
        account.setYearsExperience(request.getYearsExperience());
        return account;
    }

    public AccountResponseDTO toResponse(Account entity) {
        if (entity == null) {
            return null;
        }
        return AccountResponseDTO.builder()
                .idUser(entity.getIdUser())
                .name(entity.getName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .role(entity.getRole())
                .specialty(entity.getSpecialty())
                .portfolioUrl(entity.getPortfolioUrl())
                .bio(entity.getBio())
                .yearsExperience(entity.getYearsExperience())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public void updateEntityFromRequest(AccountRequestDTO request, Account entity) {
        entity.setName(request.getName());
        entity.setLastName(request.getLastName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setPassword(request.getPassword());
        entity.setRole(request.getRole());
        entity.setSpecialty(request.getSpecialty());
        entity.setPortfolioUrl(request.getPortfolioUrl());
        entity.setBio(request.getBio());
        entity.setYearsExperience(request.getYearsExperience());
    }
}
