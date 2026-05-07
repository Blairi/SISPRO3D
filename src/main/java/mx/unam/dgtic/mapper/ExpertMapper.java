package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.ExpertDTO;
import mx.unam.dgtic.entities.ExpertEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ExpertMapper {
    private ExpertMapper() {}

    public static ExpertDTO toDTO(ExpertEntity entity) {
        if (entity == null) {
            return null;
        }
        return ExpertDTO.builder()
                .account(AccountMapper.toDTO(entity.getAccount()))
                .specialty(entity.getSpecialty())
                .portfolioUrl(entity.getPortfolioUrl())
                .bio(entity.getBio())
                .yearsExperience(entity.getYearsExperience() != null ? entity.getYearsExperience() : 0)
                .build();
    }

    public static List<ExpertDTO> toDtoList(List<ExpertEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(ExpertMapper::toDTO).collect(Collectors.toList());
    }

    public static ExpertEntity toEntity(ExpertDTO dto) {
        if (dto == null) {
            return null;
        }
        ExpertEntity entity = new ExpertEntity();
        if (dto.getAccount() != null) {
            entity.setIdUser(dto.getAccount().getIdUser());
        }
        entity.setAccount(AccountMapper.toEntity(dto.getAccount()));
        entity.setSpecialty(dto.getSpecialty());
        entity.setPortfolioUrl(dto.getPortfolioUrl());
        entity.setBio(dto.getBio());
        entity.setYearsExperience(dto.getYearsExperience());
        return entity;
    }

    public static List<ExpertEntity> toEntityList(List<ExpertDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(ExpertMapper::toEntity).collect(Collectors.toList());
    }
}
