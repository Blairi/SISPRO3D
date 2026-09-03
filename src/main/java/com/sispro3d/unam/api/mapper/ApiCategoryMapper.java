package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.CategoryRequestDTO;
import com.sispro3d.unam.api.dto.CategoryResponseDTO;
import com.sispro3d.unam.category.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class ApiCategoryMapper {

    public Category toEntity(CategoryRequestDTO request) {
        if (request == null) {
            return null;
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return category;
    }

    public CategoryResponseDTO toResponse(Category entity) {
        if (entity == null) {
            return null;
        }
        return CategoryResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    public void updateEntityFromRequest(CategoryRequestDTO request, Category entity) {
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
    }
}
