package com.sispro3d.unam.message.service;

import com.sispro3d.unam.category.dto.CategoryRequest;
import com.sispro3d.unam.category.dto.CategoryResponse;
import com.sispro3d.unam.category.service.CategoryService;
import com.sispro3d.unam.message.dto.MessageRequest;
import com.sispro3d.unam.message.dto.MessageResponse;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceRequest;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.thread.dao.ThreadJdbcDAO;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;
import com.sispro3d.unam.user.service.AccountService;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
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
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OfferedServiceService offeredServiceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ThreadJdbcDAO threadJdbcDAO;

    private int threadId;
    private int accountId;
    private int workOrderId;
    private MessageResponse created;

    @BeforeEach
    void setUp() {
        CategoryResponse category = categoryService.create(CategoryRequest.builder()
                .name("Test-Cat-Msg")
                .description("Test-Desc-Msg")
                .build());

        AccountResponse expert = accountService.create(AccountRequest.builder()
                .name("Test-Exp-Msg")
                .lastName("Test-Ape-Msg")
                .email("test-exp-msg@unam.mx")
                .phone("+52 88888888")
                .password("test-pass")
                .role(Role.EXPERT)
                .specialty("Test-Esp-Msg")
                .build());

        OfferedServiceResponse service = offeredServiceService.create(OfferedServiceRequest.builder()
                .title("Test-Serv-Msg")
                .description("Test-Desc-Msg")
                .basePrice(new BigDecimal("150.00"))
                .expertId(expert.getIdUser())
                .categoryId(category.getId())
                .deliveryTimeDays(3)
                .build());

        AccountResponse client = accountService.create(AccountRequest.builder()
                .name("Test-Cli-Msg")
                .lastName("Test-Ape-Cli-Msg")
                .email("test-cli-msg@unam.mx")
                .phone("+52 99999999")
                .password("test-pass")
                .role(Role.CLIENT)
                .build());
        accountId = client.getIdUser();

        QuoteResponse quote = quoteService.create(QuoteRequest.builder()
                .status("PENDING")
                .totalAmount(new BigDecimal("150.00"))
                .validUntil(LocalDate.now().plusDays(30))
                .description("Test-Msg-Quote")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build());

        WorkOrderResponse workOrder = workOrderService.create(WorkOrderRequest.builder()
                .status("PENDING")
                .quoteId(quote.getId())
                .build());
        workOrderId = workOrder.getId();

        Thread thread = new Thread();
        thread.setWorkOrder(new com.sispro3d.unam.workorder.domain.WorkOrder(workOrderId));
        threadId = threadJdbcDAO.insert(thread);

        created = messageService.create(MessageRequest.builder()
                .content("Test-Message-Content")
                .threadId(threadId)
                .accountId(accountId)
                .build());
    }

    @AfterEach
    void tearDown() {
        try { messageService.delete((long) created.getId()); } catch (Exception ignored) {}
        try { threadJdbcDAO.delete(threadId); } catch (Exception ignored) {}
        try { workOrderService.delete((long) workOrderId); } catch (Exception ignored) {}
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
        assertThat(created.getId()).isPositive();
        assertThat(created.getContent()).isEqualTo("Test-Message-Content");
    }

    @Test
    void findById_shouldReturnCreatedMessage() {
        var found = messageService.findById((long) created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("Test-Message-Content");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        var result = messageService.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldContainCreatedMessage() {
        List<MessageResponse> all = messageService.findAll();

        assertThat(all)
                .filteredOn(m -> m.getId() == created.getId())
                .singleElement()
                .matches(m -> m.getContent().equals("Test-Message-Content"));
    }

    @Test
    void update_shouldModifyMessage() {
        var updated = messageService.update((long) created.getId(),
                MessageRequest.builder()
                        .content("Test-Updated")
                        .threadId(threadId)
                        .accountId(accountId)
                        .build());

        assertThat(updated.getContent()).isEqualTo("Test-Updated");

        var reloaded = messageService.findById((long) created.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getContent()).isEqualTo("Test-Updated");
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> messageService.update(99999L,
                MessageRequest.builder().content("X").build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void delete_shouldRemoveMessage() {
        messageService.delete((long) created.getId());

        assertThat(messageService.findById((long) created.getId())).isEmpty();
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> messageService.delete(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void existsById_shouldReturnTrueForExisting() {
        assertThat(messageService.existsById((long) created.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseForNonExisting() {
        assertThat(messageService.existsById(99999L)).isFalse();
    }
}
