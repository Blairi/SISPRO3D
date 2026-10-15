package com.sispro3d.unam.deliverable.repository;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.deliverable.domain.Deliverable;
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
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeliverableRepositoryTest {

    @Autowired
    private DeliverableRepository deliverableRepository;

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

    private Deliverable createDeliverable(WorkOrder order) {
        var deliverable = new Deliverable();
        deliverable.setName("Modelo FBX");
        deliverable.setUrlFile("https://files.render3d.mx/modelo.fbx");
        deliverable.setFileType("model/fbx");
        deliverable.setCreatedAt(LocalDateTime.now());
        deliverable.setWorkOrder(order);
        return deliverableRepository.save(deliverable);
    }

    @Test
    void save() {
        var expert = createExpert("deliverable.expert@sispro3d.com");
        var client = createClient("deliverable.client@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        var quote = createAcceptedQuote(client, service);
        var order = createOrder(quote);
        var deliverable = createDeliverable(order);

        assertThat(deliverable.getId()).isNotNull();
        assertThat(deliverable.getName()).isEqualTo("Modelo FBX");
        assertThat(deliverable.getWorkOrder().getId()).isEqualTo(order.getId());
    }

    @Test
    void findByWorkOrder() {
        var expert = createExpert("deliverable.expert2@sispro3d.com");
        var client = createClient("deliverable.client2@sispro3d.com");
        var service = createApprovedService("Texturizado PBR", expert);
        var quote = createAcceptedQuote(client, service);
        var order = createOrder(quote);
        createDeliverable(order);

        List<Deliverable> result = deliverableRepository.findByWorkOrder(order);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(d -> d.getWorkOrder().getId().equals(order.getId()));
    }

    @Test
    void findByWorkOrder_Id() {
        var expert = createExpert("deliverable.expert3@sispro3d.com");
        var client = createClient("deliverable.client3@sispro3d.com");
        var service = createApprovedService("Animacion de personaje", expert);
        var quote = createAcceptedQuote(client, service);
        var order = createOrder(quote);
        createDeliverable(order);

        List<Deliverable> result = deliverableRepository.findByWorkOrder_Id(order.getId());

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(d -> d.getWorkOrder().getId().equals(order.getId()));
    }
}
