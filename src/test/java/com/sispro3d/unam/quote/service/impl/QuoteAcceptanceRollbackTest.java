package com.sispro3d.unam.quote.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
class QuoteAcceptanceRollbackTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * Esta clase no es transaccional a propósito: el rollback que verifica el
     * servicio tiene que ocurrir en su propia transacción. Por eso limpia lo
     * que confirma para no contaminar el resto de la suite.
     */
    @AfterEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            quoteRepository.deleteAll();
            offeredServiceRepository.deleteAll();
            categoryRepository.deleteAll();
            accountRepository.deleteAll();
        });
    }

    @MockBean
    private WorkOrderService workOrderService;

    @Test
    void accept_whenWorkOrderCreationFails_leavesQuotePendingAndNoOrderRow() {
        var expert = createAccount("rollback.expert@sispro3d.com", Role.EXPERT);
        var client = createAccount("rollback.client@sispro3d.com", Role.CLIENT);
        var service = createApprovedService("Servicio con fallo", expert);

        var quote = quoteService.create(QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Cotización para probar rollback")
                .totalAmount(new BigDecimal("1500.00"))
                .build());

        given(workOrderService.create(any()))
                .willThrow(new IllegalStateException("Fallo simulado al crear la orden de trabajo"));

        assertThatThrownBy(() -> quoteService.accept(quote.getId(), client.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fallo simulado");

        assertThat(quoteRepository.findById(quote.getId()).orElseThrow().getStatus())
                .isEqualTo(QuoteStatus.PENDING);
        assertThat(workOrderRepository.findByQuote_Id(quote.getId())).isEmpty();
    }

    private Account createAccount(String email, Role role) {
        var account = new Account();
        account.setName("Test");
        account.setLastName("Rollback");
        account.setEmail(email);
        account.setPassword("test123");
        account.setRole(role);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private OfferedService createApprovedService(String title, Account expert) {
        var category = new Category();
        category.setName("Cat " + title);
        category.setDescription("Test category");
        categoryRepository.save(category);

        var service = new OfferedService();
        service.setTitle(title);
        service.setDescription("Test description");
        service.setBasePrice(new BigDecimal("1500.00"));
        service.setExpert(expert);
        service.setCategory(category);
        service.setStatus(ServiceStatus.APPROVED);
        service.setDeliveryTimeDays(7);
        service.setCreatedAt(LocalDateTime.now());
        return offeredServiceRepository.save(service);
    }
}