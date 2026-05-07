package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.ThreadDTO;
import mx.unam.dgtic.entities.ThreadEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ThreadMapper {
    private ThreadMapper() {}

    public static ThreadDTO toDTO(ThreadEntity entity) {
        if (entity == null) {
            return null;
        }
        return ThreadDTO.builder()
                .id(entity.getId())
                .workOrder(WorkOrderMapper.toDTO(entity.getWorkOrder()))
                .build();
    }

    public static List<ThreadDTO> toDtoList(List<ThreadEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(ThreadMapper::toDTO).collect(Collectors.toList());
    }

    public static ThreadEntity toEntity(ThreadDTO dto) {
        if (dto == null) {
            return null;
        }
        ThreadEntity entity = new ThreadEntity();
        entity.setId(dto.getId());
        entity.setWorkOrder(WorkOrderMapper.toEntity(dto.getWorkOrder()));
        return entity;
    }

    public static List<ThreadEntity> toEntityList(List<ThreadDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(ThreadMapper::toEntity).collect(Collectors.toList());
    }
}
