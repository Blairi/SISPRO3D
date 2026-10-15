package com.sispro3d.unam.category.service.impl;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceImplTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    void create() {
        var request = new CategoryRequest();
        request.setName("Impresión 3D");
        request.setDescription("Servicios de impresión");

        CategoryResponse res = categoryService.create(request);

        assertThat(res.getId()).isNotNull();
        assertThat(res.getName()).isEqualTo("Impresión 3D");
    }

    @Test
    void findAll() {
        var request = new CategoryRequest();
        request.setName("Escaneado 3D");
        categoryService.create(request);

        List<CategoryResponse> res = categoryService.findAll();

        assertThat(res).isNotEmpty();
        assertThat(res).anyMatch(c -> c.getName().equals("Escaneado 3D"));
    }

    @Test
    void findById_whenExists() {
        var request = new CategoryRequest();
        request.setName("Modelado");
        CategoryResponse created = categoryService.create(request);

        Optional<CategoryResponse> res = categoryService.findById(created.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getId()).isEqualTo(created.getId());
    }

    @Test
    void findById_whenNotExists() {
        Optional<CategoryResponse> res = categoryService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void update_whenExists() {
        var request = new CategoryRequest();
        request.setName("Original");
        CategoryResponse created = categoryService.create(request);

        var updateRequest = new CategoryRequest();
        updateRequest.setName("Actualizada");
        updateRequest.setDescription("Descripción nueva");

        CategoryResponse updated = categoryService.update(created.getId(), updateRequest);

        assertThat(updated.getName()).isEqualTo("Actualizada");
        assertThat(updated.getDescription()).isEqualTo("Descripción nueva");
    }

    @Test
    void update_whenNotExists() {
        var request = new CategoryRequest();
        request.setName("Nada");

        assertThatThrownBy(() -> categoryService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var request = new CategoryRequest();
        request.setName("Borrar");
        CategoryResponse created = categoryService.create(request);

        categoryService.delete(created.getId());

        assertThat(categoryService.findById(created.getId())).isEmpty();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> categoryService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var request = new CategoryRequest();
        request.setName("Existe");
        CategoryResponse created = categoryService.create(request);

        assertThat(categoryService.existsById(created.getId())).isTrue();
    }
}
