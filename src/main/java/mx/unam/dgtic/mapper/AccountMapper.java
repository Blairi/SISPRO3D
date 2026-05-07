package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.AccountDTO;
import mx.unam.dgtic.entities.AccountEntity;

import java.util.List;

public class AccountMapper {

    private AccountMapper() {
    }

    public static AccountDTO toDTO(AccountEntity entity) {

        if (entity == null) {
            return null;
        }

        return AccountDTO.builder()
                .idUser(entity.getIdUser())
                .name(entity.getName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .password(entity.getPassword())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static List<AccountDTO> toDtoList(List<AccountEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(AccountMapper::toDTO)
                .toList();
    }

    public static AccountEntity toEntity(AccountDTO dto) {

        if (dto == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();

        entity.setIdUser(dto.getIdUser());
        entity.setName(dto.getName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setPassword(dto.getPassword());
        entity.setType(dto.getType());
        entity.setCreatedAt(dto.getCreatedAt());

        return entity;
    }

    public static List<AccountEntity> toEntityList(List<AccountDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(AccountMapper::toEntity)
                .toList();
    }
}