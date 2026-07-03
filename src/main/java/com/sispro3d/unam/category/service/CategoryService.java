package com.sispro3d.unam.category.service;

import com.sispro3d.unam.category.dto.CategoryDTO;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryDTO> findAll();
    Optional<CategoryDTO> findById(int id);
    CategoryDTO create(CategoryDTO dto);
    CategoryDTO update(int id, CategoryDTO dto);
    void delete(int id);
}
