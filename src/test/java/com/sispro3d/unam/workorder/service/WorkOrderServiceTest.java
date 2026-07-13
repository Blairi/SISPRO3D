package com.sispro3d.unam.workorder.service;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class WorkOrderServiceTest {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private QuoteService quoteService;

    private int quoteId;
    private WorkOrderResponse created;

    @BeforeEach
    void setUp() {
        CategoryResponse category = categoryService.create(CategoryRequest.builder()
                .name("Test-Cat-WO")
                .description("Test-Desc-WO")
                .build());

        AccountResponse expert = accountService.create(AccountRequest.builder()
                .name("Test-Exp-WO")
                .lastName("Test-Ape-WO")
                .email("test-exp-wo@unam.mx")
                .phone("+52 44444444")
                .password("test-pass")
                .role(Role.EXPERT)
                .specialty("Test-Esp-WO")
                .build());

        OfferedServiceResponse service = offeredServiceService.create(OfferedServiceRequest.builder()
                .title("Test-Serv-WO")
                .description("Test-Desc-WO")
                .basePrice(new BigDecimal("300.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .deliveryTimeDays(5)
                .build());

        AccountResponse client = accountService.create(AccountRequest.builder()
                .name("Test-Cli-WO")
                .lastName("Test-Ape-Cli-WO")
                .email("test-cli-wo@unam.mx")
                .phone("+52 55555555")
                .password("test-pass")
                .role(Role.CLIENT)
                .build());

        QuoteResponse quote = quoteService.create(QuoteRequest.builder()
                .status("PENDING")
                .totalAmount(new BigDecimal("300.00"))
                .validUntil(LocalDate.now().plusDays(30))
                .description("Test-WO-Quote")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build());
        quoteId = quote.getId();

        created = workOrderService.create(WorkOrderRequest.builder()
                .status("PENDING")
                .quoteId(quoteId)
                .build());
    }

    @AfterEach
    void tearDown() {
        try { workOrderService.delete((long) created.getId()); } catch (Exception ignored) {}
        try { quoteService.delete((long) quoteId); } catch (Exception ignored) {}
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
        assertThat(created.getId()).isPositive();
        assertThat(created.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void findById_shouldReturnCreatedWorkOrder() {
        var found = workOrderService.findById((long) created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("PENDING");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        var result = workOrderService.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldContainCreatedWorkOrder() {
        List<WorkOrderResponse> all = workOrderService.findAll();

        assertThat(all)
                .filteredOn(w -> w.getId() == created.getId())
                .singleElement()
                .matches(w -> w.getStatus().equals("PENDING"));
    }

    @Test
    void update_shouldModifyWorkOrder() {
        var updated = workOrderService.update((long) created.getId(),
                WorkOrderRequest.builder()
                        .status("COMPLETED")
                        .quoteId(quoteId)
                        .build());

        assertThat(updated.getStatus()).isEqualTo("COMPLETED");

        var reloaded = workOrderService.findById((long) created.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> workOrderService.update(99999L,
                WorkOrderRequest.builder().status("X").build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void delete_shouldRemoveWorkOrder() {
        workOrderService.delete((long) created.getId());

        assertThat(workOrderService.findById((long) created.getId())).isEmpty();
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> workOrderService.delete(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void existsById_shouldReturnTrueForExisting() {
        assertThat(workOrderService.existsById((long) created.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseForNonExisting() {
        assertThat(workOrderService.existsById(99999L)).isFalse();
    }
}
