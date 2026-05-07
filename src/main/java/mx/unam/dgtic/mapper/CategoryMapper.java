package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.CategoryDTO;
import mx.unam.dgtic.entities.CategoryEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryMapper {
    private CategoryMapper() {}

    public static CategoryDTO toDTO(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public static List<CategoryDTO> toDtoList(List<CategoryEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(CategoryMapper::toDTO).collect(Collectors.toList());
    }

    public static CategoryEntity toEntity(CategoryDTO dto) {
        if (dto == null) {
            return null;
        }
        CategoryEntity entity = new CategoryEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    public static List<CategoryEntity> toEntityList(List<CategoryDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(CategoryMapper::toEntity).collect(Collectors.toList());
    }
}
