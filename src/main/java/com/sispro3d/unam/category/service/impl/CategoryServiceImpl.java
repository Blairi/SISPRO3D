package com.sispro3d.unam.category.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.category.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<CategoryResponse> findById(Long id) {
        return categoryRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        Category category = toEntity(request);
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        int pk = id.intValue();
        categoryRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));

        Category category = toEntity(request);
        category.setId(pk);
        Category updated = categoryRepository.update(category);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        categoryRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        categoryRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id.intValue());
    }

    private Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return category;
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
