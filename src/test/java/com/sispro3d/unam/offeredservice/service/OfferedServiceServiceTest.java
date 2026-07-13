package com.sispro3d.unam.offeredservice.service;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OfferedServiceServiceTest {

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    private CategoryResponse category;
    private int expertId;
    private OfferedServiceResponse created;

    @BeforeEach
    void setUp() {
        category = categoryService.create(CategoryRequest.builder()
                .name("Test-Cat-Servicio")
                .description("Test-Desc-Servicio")
                .build());

        AccountResponse expertAccount = accountService.create(AccountRequest.builder()
                .name("Test-Experto")
                .lastName("Test-Apellido")
                .email("test-experto-servicio@unam.mx")
                .phone("+52 11111111")
                .password("test-pass")
                .role(Role.EXPERT)
                .specialty("Test-Especialidad")
                .build());
        expertId = expertAccount.getIdUser();

        created = offeredServiceService.create(OfferedServiceRequest.builder()
                .title("Test-Servicio")
                .description("Test-Desc-Servicio")
                .basePrice(new BigDecimal("999.99"))
                .expertId(expertId)
                .categoryId(category.getId())
                .deliveryTimeDays(10)
                .build());
    }

    @AfterEach
    void tearDown() {
        try {
            offeredServiceService.delete((long) created.getId());
        } catch (Exception ignored) {
        }
        try {
            accountService.delete((long) expertId);
        } catch (Exception ignored) {
        }
        try {
            categoryService.delete((long) category.getId());
        } catch (Exception ignored) {
        }
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
        assertThat(created.getId()).isPositive();
        assertThat(created.getTitle()).isEqualTo("Test-Servicio");
        assertThat(created.getBasePrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(created.getDeliveryTimeDays()).isEqualTo(10);
    }

    @Test
    void findById_shouldReturnCreatedService() {
        var found = offeredServiceService.findById((long) created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test-Servicio");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        var result = offeredServiceService.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldContainCreatedService() {
        List<OfferedServiceResponse> all = offeredServiceService.findAll();

        assertThat(all)
                .filteredOn(s -> s.getId() == created.getId())
                .singleElement()
                .matches(s -> s.getTitle().equals("Test-Servicio"));
    }

    @Test
    void update_shouldModifyService() {
        var updated = offeredServiceService.update((long) created.getId(),
                OfferedServiceRequest.builder()
                        .title("Test-Actualizado")
                        .description(created.getDescription())
                        .basePrice(new BigDecimal("1999.99"))
                        .expertId(expertId)
                        .categoryId(category.getId())
                        .deliveryTimeDays(20)
                        .build());

        assertThat(updated.getTitle()).isEqualTo("Test-Actualizado");
        assertThat(updated.getBasePrice()).isEqualByComparingTo(new BigDecimal("1999.99"));

        var reloaded = offeredServiceService.findById((long) created.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getTitle()).isEqualTo("Test-Actualizado");
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> offeredServiceService.update(99999L,
                OfferedServiceRequest.builder().title("X").build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void delete_shouldRemoveService() {
        offeredServiceService.delete((long) created.getId());

        assertThat(offeredServiceService.findById((long) created.getId())).isEmpty();
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> offeredServiceService.delete(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void existsById_shouldReturnTrueForExisting() {
        assertThat(offeredServiceService.existsById((long) created.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseForNonExisting() {
        assertThat(offeredServiceService.existsById(99999L)).isFalse();
    }
}
