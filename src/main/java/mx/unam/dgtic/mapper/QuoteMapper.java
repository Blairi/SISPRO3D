package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.QuoteDTO;
import mx.unam.dgtic.entities.QuoteEntity;
import mx.unam.dgtic.domain.QuoteStatus;

import java.util.List;
import java.util.stream.Collectors;

public class QuoteMapper {
    private QuoteMapper() {}

    public static QuoteDTO toDTO(QuoteEntity entity) {
        if (entity == null) {
            return null;
        }
        return QuoteDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .totalAmount(entity.getTotalAmount())
                .validUntil(entity.getValidUntil())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .client(ClientMapper.toDTO(entity.getClient()))
                .service(ServiceMapper.toDTO(entity.getService()))
                .build();
    }

    public static List<QuoteDTO> toDtoList(List<QuoteEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(QuoteMapper::toDTO).collect(Collectors.toList());
    }

    public static QuoteEntity toEntity(QuoteDTO dto) {
        if (dto == null) {
            return null;
        }
        QuoteEntity entity = new QuoteEntity();
        entity.setId(dto.getId());
        entity.setStatus(dto.getStatus() != null ? QuoteStatus.valueOf(dto.getStatus()) : null);
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setValidUntil(dto.getValidUntil());
        entity.setDescription(dto.getDescription());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setClient(ClientMapper.toEntity(dto.getClient()));
        entity.setService(ServiceMapper.toEntity(dto.getService()));
        return entity;
    }

    public static List<QuoteEntity> toEntityList(List<QuoteDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(QuoteMapper::toEntity).collect(Collectors.toList());
    }
}
