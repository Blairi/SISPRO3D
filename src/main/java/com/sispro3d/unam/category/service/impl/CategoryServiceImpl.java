package com.sispro3d.unam.category.service.impl;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.mapper.CategoryMapper;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<CategoryResponse> findById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse);
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        var category = categoryMapper.toEntity(request);
        var saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        var existing = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Category", id));

        categoryMapper.updateEntityFromRequest(request, existing);
        var updated = categoryRepository.save(existing);
        return categoryMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Category", id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }
}
