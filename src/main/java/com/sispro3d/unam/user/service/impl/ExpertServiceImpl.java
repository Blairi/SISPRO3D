package com.sispro3d.unam.user.service.impl;

import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Expert;
import com.sispro3d.unam.user.dto.ExpertRequest;
import com.sispro3d.unam.user.dto.ExpertResponse;
import com.sispro3d.unam.user.repository.ExpertRepository;
import com.sispro3d.unam.user.service.ExpertService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpertServiceImpl implements ExpertService {

    private final ExpertRepository expertRepository;

    public ExpertServiceImpl(ExpertRepository expertRepository) {
        this.expertRepository = expertRepository;
    }

    @Override
    public List<ExpertResponse> findAll() {
        return expertRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<ExpertResponse> findById(Long id) {
        return expertRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public ExpertResponse create(ExpertRequest request) {
        Expert expert = toEntity(request);
        Expert saved = expertRepository.save(expert);
        return toResponse(saved);
    }

    @Override
    public ExpertResponse update(Long id, ExpertRequest request) {
        int pk = id.intValue();
        expertRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Experto no encontrado con id: " + id));

        Expert expert = toEntity(request);
        expert.getAccount().setIdUser(pk);
        Expert updated = expertRepository.update(expert);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        expertRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Experto no encontrado con id: " + id));
        expertRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return expertRepository.existsById(id.intValue());
    }

    private Expert toEntity(ExpertRequest request) {
        Expert expert = new Expert();
        expert.setAccount(new Account(request.getAccountId()));
        expert.setSpecialty(request.getSpecialty());
        expert.setPortfolioUrl(request.getPortfolioUrl());
        expert.setBio(request.getBio());
        expert.setYearsExperience(request.getYearsExperience());
        return expert;
    }

    private ExpertResponse toResponse(Expert expert) {
        ExpertResponse.ExpertResponseBuilder builder = ExpertResponse.builder();

        if (expert.getAccount() != null) {
            builder.id(expert.getAccount().getIdUser())
                    .name(expert.getAccount().getName())
                    .lastName(expert.getAccount().getLastName())
                    .email(expert.getAccount().getEmail());
        }

        builder.specialty(expert.getSpecialty())
                .portfolioUrl(expert.getPortfolioUrl())
                .bio(expert.getBio())
                .yearsExperience(expert.getYearsExperience());

        return builder.build();
    }
}
