package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.WorkOrderDTO;
import mx.unam.dgtic.entities.WorkOrderEntity;
import mx.unam.dgtic.domain.OrderStatus;

import java.util.List;
import java.util.stream.Collectors;

public class WorkOrderMapper {
    private WorkOrderMapper() {}

    public static WorkOrderDTO toDTO(WorkOrderEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkOrderDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .quote(QuoteMapper.toDTO(entity.getQuote()))
                .build();
    }

    public static List<WorkOrderDTO> toDtoList(List<WorkOrderEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(WorkOrderMapper::toDTO).collect(Collectors.toList());
    }

    public static WorkOrderEntity toEntity(WorkOrderDTO dto) {
        if (dto == null) {
            return null;
        }
        WorkOrderEntity entity = new WorkOrderEntity();
        entity.setId(dto.getId());
        entity.setStatus(dto.getStatus() != null ? OrderStatus.valueOf(dto.getStatus()) : null);
        entity.setStartedAt(dto.getStartedAt());
        entity.setCompletedAt(dto.getCompletedAt());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setQuote(QuoteMapper.toEntity(dto.getQuote()));
        return entity;
    }

    public static List<WorkOrderEntity> toEntityList(List<WorkOrderDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(WorkOrderMapper::toEntity).collect(Collectors.toList());
    }
}
