package com.sispro3d.unam.workorder.repository;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkOrderRepositoryTest {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private QuoteRepository quoteRepository;

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
        order.setStatus(WorkOrderStatus.PENDING);
        order.setQuote(quote);
        order.setCreatedAt(LocalDateTime.now());
        return workOrderRepository.save(order);
    }

    @Test
    void save() {
        var expert = createExpert("workorder.expert@sispro3d.com");
        var client = createClient("workorder.client@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        var quote = createAcceptedQuote(client, service);
        var order = createOrder(quote);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.PENDING);
        assertThat(order.getQuote().getId()).isEqualTo(quote.getId());
    }

    @Test
    void findByQuote() {
        var expert = createExpert("workorder.expert2@sispro3d.com");
        var client = createClient("workorder.client2@sispro3d.com");
        var service = createApprovedService("Texturizado PBR", expert);
        var quote = createAcceptedQuote(client, service);
        createOrder(quote);

        Optional<WorkOrder> result = workOrderRepository.findByQuote(quote);

        assertThat(result).isPresent();
        assertThat(result.get().getQuote().getId()).isEqualTo(quote.getId());
    }

    @Test
    void findByStatus() {
        var expert = createExpert("workorder.expert3@sispro3d.com");
        var client = createClient("workorder.client3@sispro3d.com");
        var service = createApprovedService("Animacion de personaje", expert);
        var quote = createAcceptedQuote(client, service);
        createOrder(quote);

        List<WorkOrder> result = workOrderRepository.findByStatus(WorkOrderStatus.PENDING);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(o -> o.getStatus() == WorkOrderStatus.PENDING);
    }

    @Test
    void findByQuote_Client_IdUser() {
        var expert = createExpert("workorder.expert4@sispro3d.com");
        var client = createClient("workorder.client4@sispro3d.com");
        var service = createApprovedService("Rigging completo", expert);
        var quote = createAcceptedQuote(client, service);
        createOrder(quote);

        List<WorkOrder> result = workOrderRepository.findByQuote_Client_IdUser(client.getIdUser());

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(o -> o.getQuote().getClient().getIdUser().equals(client.getIdUser()));
    }

    @Test
    void findByQuote_OfferedService_Expert_IdUser() {
        var expert = createExpert("workorder.expert5@sispro3d.com");
        var client = createClient("workorder.client5@sispro3d.com");
        var service = createApprovedService("Render final", expert);
        var quote = createAcceptedQuote(client, service);
        createOrder(quote);

        List<WorkOrder> result = workOrderRepository.findByQuote_OfferedService_Expert_IdUser(expert.getIdUser());

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(o ->
                o.getQuote().getOfferedService().getExpert().getIdUser().equals(expert.getIdUser()));
    }
}
