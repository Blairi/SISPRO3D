package com.sispro3d.unam.thread.repository;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ThreadRepositoryTest {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    private Account createExpert(String email) {
        var account = new Account();
        account.setName("Expert");
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword("test123");
        account.setRole(Role.EXPERT);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private Account createClient(String email) {
        var account = new Account();
        account.setName("Client");
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword("test123");
        account.setRole(Role.CLIENT);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private Category createCategory(String name) {
        var category = new Category();
        category.setName(name);
        category.setDescription("Test category");
        return categoryRepository.save(category);
    }

    private OfferedService createApprovedService(String title, Account expert) {
        var category = createCategory("Cat " + title);
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

    private Quote createAcceptedQuote(Account client, OfferedService service) {
        var quote = new Quote();
        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setTotalAmount(new BigDecimal("1500.00"));
        quote.setDescription("Test quote");
        quote.setClient(client);
        quote.setOfferedService(service);
        quote.setCreatedAt(LocalDateTime.now());
        return quoteRepository.save(quote);
    }

    private WorkOrder createOrder(Quote quote) {
        var order = new WorkOrder();
        order.setStatus(WorkOrderStatus.IN_PROGRESS);
        order.setQuote(quote);
        order.setCreatedAt(LocalDateTime.now());
        return workOrderRepository.save(order);
    }

    private Thread createThread(WorkOrder order) {
        var thread = new Thread();
        thread.setWorkOrder(order);
        return threadRepository.save(thread);
    }

    @Test
    void save() {
        var expert = createExpert("thread.expert@sispro3d.com");
        var client = createClient("thread.client@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        var quote = createAcceptedQuote(client, service);
        var order = createOrder(quote);
        var thread = createThread(order);

        assertThat(thread.getId()).isNotNull();
        assertThat(thread.getWorkOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    void findByWorkOrder() {
        var expert = createExpert("thread.expert2@sispro3d.com");
        var client = createClient("thread.client2@sispro3d.com");
        var service = createApprovedService("Texturizado PBR", expert);
        var quote = createAcceptedQuote(client, service);
        var order = createOrder(quote);
        createThread(order);

        Optional<Thread> result = threadRepository.findByWorkOrder(order);

        assertThat(result).isPresent();
        assertThat(result.get().getWorkOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    void findByWorkOrder_Id() {
        var expert = createExpert("thread.expert3@sispro3d.com");
        var client = createClient("thread.client3@sispro3d.com");
        var service = createApprovedService("Animacion de personaje", expert);
        var quote = createAcceptedQuote(client, service);
        var order = createOrder(quote);
        createThread(order);

        Optional<Thread> result = threadRepository.findByWorkOrder_Id(order.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getWorkOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    void existsByWorkOrder_Id() {
        var expert = createExpert("thread.expert4@sispro3d.com");
        var client = createClient("thread.client4@sispro3d.com");
        var service = createApprovedService("Rigging completo", expert);
        var quote = createAcceptedQuote(client, service);
        var order = createOrder(quote);
        createThread(order);

        assertThat(threadRepository.existsByWorkOrder_Id(order.getId())).isTrue();
    }
}
