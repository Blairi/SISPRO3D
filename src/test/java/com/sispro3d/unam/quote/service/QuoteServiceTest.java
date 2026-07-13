package com.sispro3d.unam.quote.service;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
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
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class QuoteServiceTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private AccountService accountService;

    private CategoryResponse category;
    private int expertId;
    private int clientId;
    private int offeredServiceId;
    private QuoteResponse created;

    @BeforeEach
    void setUp() {
        category = categoryService.create(CategoryRequest.builder()
                .name("Test-Cat-Quote")
                .description("Test-Desc-Quote")
                .build());

        AccountResponse expertAccount = accountService.create(AccountRequest.builder()
                .name("Test-Exp-Quote")
                .lastName("Test-Ape-Quote")
                .email("test-exp-quote@unam.mx")
                .phone("+52 22222222")
                .password("test-pass")
                .role(Role.EXPERT)
                .specialty("Test-Esp-Quote")
                .build());
        expertId = expertAccount.getIdUser();

        OfferedServiceResponse service = offeredServiceService.create(OfferedServiceRequest.builder()
                .title("Test-Serv-Quote")
                .description("Test-Desc-Quote")
                .basePrice(new BigDecimal("500.00"))
                .expertId(expertId)
                .categoryId(category.getId())
                .deliveryTimeDays(5)
                .build());
        offeredServiceId = service.getId();

        AccountResponse clientAccount = accountService.create(AccountRequest.builder()
                .name("Test-Cli-Quote")
                .lastName("Test-Ape-Cli")
                .email("test-cli-quote@unam.mx")
                .phone("+52 33333333")
                .password("test-pass")
                .role(Role.CLIENT)
                .build());
        clientId = clientAccount.getIdUser();

        created = quoteService.create(QuoteRequest.builder()
                .status("PENDING")
                .totalAmount(new BigDecimal("500.00"))
                .validUntil(LocalDate.now().plusDays(30))
                .description("Test-Quote-Desc")
                .clientId(clientId)
                .offeredServiceId(offeredServiceId)
                .build());
    }

    @AfterEach
    void tearDown() {
        try { quoteService.delete((long) created.getId()); } catch (Exception ignored) {}
        try { accountService.delete((long) clientId); } catch (Exception ignored) {}
        try { offeredServiceService.delete((long) offeredServiceId); } catch (Exception ignored) {}
        try { accountService.delete((long) expertId); } catch (Exception ignored) {}
        try { categoryService.delete((long) category.getId()); } catch (Exception ignored) {}
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
        assertThat(created.getId()).isPositive();
        assertThat(created.getStatus()).isEqualTo("PENDING");
        assertThat(created.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void findById_shouldReturnCreatedQuote() {
        var found = quoteService.findById((long) created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("PENDING");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        var result = quoteService.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldContainCreatedQuote() {
        List<QuoteResponse> all = quoteService.findAll();

        assertThat(all)
                .filteredOn(q -> q.getId() == created.getId())
                .singleElement()
                .matches(q -> q.getStatus().equals("PENDING"));
    }

    @Test
    void update_shouldModifyQuote() {
        var updated = quoteService.update((long) created.getId(),
                QuoteRequest.builder()
                        .status("ACCEPTED")
                        .totalAmount(new BigDecimal("600.00"))
                        .validUntil(LocalDate.now().plusDays(60))
                        .description("Test-Updated")
                        .clientId(clientId)
                        .offeredServiceId(offeredServiceId)
                        .build());

        assertThat(updated.getStatus()).isEqualTo("ACCEPTED");
        assertThat(updated.getTotalAmount()).isEqualByComparingTo(new BigDecimal("600.00"));

        var reloaded = quoteService.findById((long) created.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> quoteService.update(99999L,
                QuoteRequest.builder().status("X").build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void delete_shouldRemoveQuote() {
        quoteService.delete((long) created.getId());

        assertThat(quoteService.findById((long) created.getId())).isEmpty();
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> quoteService.delete(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void existsById_shouldReturnTrueForExisting() {
        assertThat(quoteService.existsById((long) created.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseForNonExisting() {
        assertThat(quoteService.existsById(99999L)).isFalse();
    }
}
