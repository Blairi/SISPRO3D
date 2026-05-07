package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.DeliverableDTO;
import mx.unam.dgtic.dto.WorkOrderDTO;
import mx.unam.dgtic.entities.DeliverableEntity;
import mx.unam.dgtic.entities.WorkOrderEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeliverableMapper {
    private DeliverableMapper() {}

    public static DeliverableDTO toDTO(DeliverableEntity entity) {
        if (entity == null) {
            return null;
        }
        return DeliverableDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .urlFile(entity.getUrlFile())
                .createdAt(entity.getCreatedAt())
                .fileType(entity.getFileType())
                .workOrder(WorkOrderMapper.toDTO(entity.getWorkOrder()))
                .build();
    }

    public static List<DeliverableDTO> toDtoList(List<DeliverableEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(DeliverableMapper::toDTO).collect(Collectors.toList());
    }

    public static DeliverableEntity toEntity(DeliverableDTO dto) {
        if (dto == null) {
            return null;
        }
        DeliverableEntity entity = new DeliverableEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setUrlFile(dto.getUrlFile());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setFileType(dto.getFileType());
        entity.setWorkOrder(WorkOrderMapper.toEntity(dto.getWorkOrder()));
        return entity;
    }

    public static List<DeliverableEntity> toEntityList(List<DeliverableDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(DeliverableMapper::toEntity).collect(Collectors.toList());
    }
}
