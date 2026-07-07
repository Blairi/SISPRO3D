package com.sispro3d.unam.category.service;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    private CategoryResponse created;

    @BeforeEach
    void setUp() {
        created = categoryService.create(CategoryRequest.builder()
                .name("Test-Categoría")
                .description("Test-Descripción")
                .build());
    }

    @AfterEach
    void tearDown() {
        try {
            categoryService.delete((long) created.getId());
        } catch (Exception ignored) {
        }
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("Test-Categoría");
        assertThat(created.getDescription()).isEqualTo("Test-Descripción");
    }

    @Test
    void findById_shouldReturnCreatedCategory() {
        var found = categoryService.findById((long) created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test-Categoría");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        var result = categoryService.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldContainCreatedCategory() {
        List<CategoryResponse> all = categoryService.findAll();

        assertThat(all)
                .filteredOn(c -> c.getId() == created.getId())
                .singleElement()
                .matches(c -> c.getName().equals("Test-Categoría"));
    }

    @Test
    void update_shouldModifyCategory() {
        var updated = categoryService.update((long) created.getId(),
                CategoryRequest.builder()
                        .name("Test-Actualizado")
                        .description("Test-Desc Actualizada")
                        .build());

        assertThat(updated.getName()).isEqualTo("Test-Actualizado");
        assertThat(updated.getDescription()).isEqualTo("Test-Desc Actualizada");

        var reloaded = categoryService.findById((long) created.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getName()).isEqualTo("Test-Actualizado");
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> categoryService.update(99999L,
                CategoryRequest.builder().name("X").build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void delete_shouldRemoveCategory() {
        categoryService.delete((long) created.getId());

        assertThat(categoryService.findById((long) created.getId())).isEmpty();
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> categoryService.delete(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void existsById_shouldReturnTrueForExisting() {
        assertThat(categoryService.existsById((long) created.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseForNonExisting() {
        assertThat(categoryService.existsById(99999L)).isFalse();
    }
}
