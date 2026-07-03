package com.sispro3d.unam.category.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.dto.CategoryDTO;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.core.dao.GenericDAO;

import java.util.List;
import java.util.Optional;

public class CategoryServiceImpl implements CategoryService {

    private final GenericDAO<Category> categoryDAO;

    public CategoryServiceImpl(GenericDAO<Category> categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @Override
    public List<CategoryDTO> findAll() {
        return categoryDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<CategoryDTO> findById(int id) {
        return categoryDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public CategoryDTO create(CategoryDTO dto) {
        Category category = toEntity(dto);
        int generatedId = categoryDAO.insert(category);
        category.setId(generatedId);
        return toResponseDTO(category);
    }

    @Override
    public CategoryDTO update(int id, CategoryDTO dto) {
        categoryDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));

        Category category = toEntity(dto);
        category.setId(id);
        categoryDAO.update(category);
        return toResponseDTO(category);
    }

    @Override
    public void delete(int id) {
        categoryDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        categoryDAO.delete(id);
    }

    private Category toEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    private CategoryDTO toResponseDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getDescription());
    }
}
