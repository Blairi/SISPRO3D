package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.CategoryDTO;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryDTO> findAll();
    Optional<CategoryDTO> findById(int id);
    CategoryDTO create(CategoryDTO dto);
    CategoryDTO update(int id, CategoryDTO dto);
    void delete(int id);
}
