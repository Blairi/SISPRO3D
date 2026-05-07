package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.PreviewDTO;
import mx.unam.dgtic.entities.PreviewEntity;

import java.util.List;
import java.util.stream.Collectors;

public class PreviewMapper {
    private PreviewMapper() {}

    public static PreviewDTO toDTO(PreviewEntity entity) {
        if (entity == null) {
            return null;
        }
        return PreviewDTO.builder()
                .id(entity.getId())
                .caption(entity.getCaption())
                .urlFile(entity.getUrlFile())
                .deliverable(DeliverableMapper.toDTO(entity.getDeliverable()))
                .build();
    }

    public static List<PreviewDTO> toDtoList(List<PreviewEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(PreviewMapper::toDTO).collect(Collectors.toList());
    }

    public static PreviewEntity toEntity(PreviewDTO dto) {
        if (dto == null) {
            return null;
        }
        PreviewEntity entity = new PreviewEntity();
        entity.setId(dto.getId());
        entity.setCaption(dto.getCaption());
        entity.setUrlFile(dto.getUrlFile());
        entity.setDeliverable(DeliverableMapper.toEntity(dto.getDeliverable()));
        return entity;
    }

    public static List<PreviewEntity> toEntityList(List<PreviewDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(PreviewMapper::toEntity).collect(Collectors.toList());
    }
}
