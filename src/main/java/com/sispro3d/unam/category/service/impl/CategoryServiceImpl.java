package com.sispro3d.unam.category.service.impl;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Override
    public List<CategoryResponse> findAll() {
        return List.of();
    }

    @Override
    public Optional<CategoryResponse> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        return null;
    }

    @Override
    public CategoryResponse update(Long aLong, CategoryRequest request) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }
}
