package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.CategoryRequestDTO;
import com.sispro3d.unam.api.dto.CategoryResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.mapper.ApiCategoryMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ApiCategoryService {

    private final CategoryRepository categoryRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final ApiCategoryMapper mapper;

    public ApiCategoryService(CategoryRepository categoryRepository,
                              OfferedServiceRepository offeredServiceRepository,
                              ApiCategoryMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.mapper = mapper;
    }

    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public CategoryResponseDTO findById(Long id) {
        return categoryRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("Category", id));
    }

    public CategoryResponseDTO create(CategoryRequestDTO request) {
        var saved = categoryRepository.save(mapper.toEntity(request));
        log.info("Categoría creada (API): id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    public CategoryResponseDTO update(Long id, CategoryRequestDTO request) {
        var existing = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Category", id));
        mapper.updateEntityFromRequest(request, existing);
        var updated = categoryRepository.save(existing);
        log.info("Categoría actualizada (API): id={}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        var existing = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Category", id));
        if (!offeredServiceRepository.findByCategory_Id(id).isEmpty()) {
            log.warn("Rechazado: categoría con servicios no eliminable (API): id={}", id);
            throw new DataIntegrityException("Category with id " + id
                    + " has associated offered services and cannot be deleted");
        }
        categoryRepository.delete(existing);
        log.info("Categoría eliminada (API): id={}", id);
    }
}
