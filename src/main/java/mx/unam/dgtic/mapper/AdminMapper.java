package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.AdminDTO;
import mx.unam.dgtic.entities.AdminEntity;

import java.util.List;
import java.util.stream.Collectors;

public class AdminMapper {
    private AdminMapper() {}

    public static AdminDTO toDTO(AdminEntity entity) {
        if (entity == null) {
            return null;
        }
        return AdminDTO.builder()
                .account(AccountMapper.toDTO(entity.getAccount()))
                .build();
    }

    public static List<AdminDTO> toDtoList(List<AdminEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(AdminMapper::toDTO).collect(Collectors.toList());
    }

    public static AdminEntity toEntity(AdminDTO dto) {
        if (dto == null) {
            return null;
        }
        AdminEntity entity = new AdminEntity();
        if (dto.getAccount() != null) {
            entity.setIdUser(dto.getAccount().getIdUser());
        }
        entity.setAccount(AccountMapper.toEntity(dto.getAccount()));
        return entity;
    }

    public static List<AdminEntity> toEntityList(List<AdminDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(AdminMapper::toEntity).collect(Collectors.toList());
    }
}
