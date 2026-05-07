package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.ClientDTO;
import mx.unam.dgtic.entities.ClientEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ClientMapper {
    private ClientMapper() {}

    public static ClientDTO toDTO(ClientEntity entity) {
        if (entity == null) {
            return null;
        }
        return ClientDTO.builder()
                .account(AccountMapper.toDTO(entity.getAccount()))
                .build();
    }

    public static List<ClientDTO> toDtoList(List<ClientEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(ClientMapper::toDTO).collect(Collectors.toList());
    }

    public static ClientEntity toEntity(ClientDTO dto) {
        if (dto == null) {
            return null;
        }
        ClientEntity entity = new ClientEntity();
        if (dto.getAccount() != null) {
            entity.setIdUser(dto.getAccount().getIdUser());
        }
        entity.setAccount(AccountMapper.toEntity(dto.getAccount()));
        return entity;
    }

    public static List<ClientEntity> toEntityList(List<ClientDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(ClientMapper::toEntity).collect(Collectors.toList());
    }
}
