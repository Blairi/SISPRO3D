package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.ServiceDTO;
import mx.unam.dgtic.entities.ServiceEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceMapper {
    private ServiceMapper() {}

    public static ServiceDTO toDTO(ServiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return ServiceDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .basePrice(entity.getBasePrice())
                .admin(AdminMapper.toDTO(entity.getAdmin()))
                .expert(ExpertMapper.toDTO(entity.getExpert()))
                .category(CategoryMapper.toDTO(entity.getCategory()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deliveryTimeDays(entity.getDeliveryTimeDays() != null ? entity.getDeliveryTimeDays() : 0)
                .build();
    }

    public static List<ServiceDTO> toDtoList(List<ServiceEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(ServiceMapper::toDTO).collect(Collectors.toList());
    }

    public static ServiceEntity toEntity(ServiceDTO dto) {
        if (dto == null) {
            return null;
        }
        ServiceEntity entity = new ServiceEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setBasePrice(dto.getBasePrice());
        entity.setAdmin(AdminMapper.toEntity(dto.getAdmin()));
        entity.setExpert(ExpertMapper.toEntity(dto.getExpert()));
        entity.setCategory(CategoryMapper.toEntity(dto.getCategory()));
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setDeliveryTimeDays(dto.getDeliveryTimeDays());
        return entity;
    }

    public static List<ServiceEntity> toEntityList(List<ServiceDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(ServiceMapper::toEntity).collect(Collectors.toList());
    }
}
