package com.sispro3d.unam.user.mapper;

import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toEntity(AccountRequest request) {
        if (request == null) {
            return null;
        }
        var account = new Account();
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

    public AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }
        return AccountResponse.builder()
                .idUser(account.getIdUser())
                .name(account.getName())
                .lastName(account.getLastName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .role(account.getRole())
                .specialty(account.getSpecialty())
                .portfolioUrl(account.getPortfolioUrl())
                .bio(account.getBio())
                .yearsExperience(account.getYearsExperience())
                .createdAt(account.getCreatedAt())
                .build();
    }

    /**
     * Applies the fields from a request onto an existing entity, useful for updates.
     * Does not touch idUser or createdAt, which are managed by the persistence layer.
     */
    public void updateEntityFromRequest(AccountRequest request, Account account) {
        account.setName(request.getName());
        account.setLastName(request.getLastName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setRole(request.getRole());
        account.setSpecialty(request.getSpecialty());
        account.setPortfolioUrl(request.getPortfolioUrl());
        account.setBio(request.getBio());
        account.setYearsExperience(request.getYearsExperience());
    }
}