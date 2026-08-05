package com.sispro3d.unam.thread.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.thread.dto.ThreadRequest;
import com.sispro3d.unam.thread.dto.ThreadResponse;
import com.sispro3d.unam.thread.repository.ThreadRepository;
import com.sispro3d.unam.thread.service.ThreadService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ThreadServiceImplTest {

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private WorkOrderService workOrderService;

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

    private WorkOrderResponse createOrder(Account client, OfferedService service) {
        var quoteRequest = QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Solicito cotización para mi proyecto")
                .totalAmount(new BigDecimal("1500.00"))
                .build();
        QuoteResponse quote = quoteService.create(quoteRequest);
        QuoteResponse accepted = quoteService.accept(quote.getId(), client.getIdUser());

        var orderRequest = WorkOrderRequest.builder()
                .clientId(client.getIdUser())
                .quoteId(accepted.getId())
                .build();
        return workOrderService.create(orderRequest);
    }

    private ThreadRequest createThreadRequest(Account actor, WorkOrderResponse order) {
        return ThreadRequest.builder()
                .actorId(actor.getIdUser())
                .workOrderId(order.getId())
                .build();
    }

    @Test
    void create_byClient() {
        var expert = createExpert("thread.expert.create@sispro3d.com");
        var client = createClient("thread.client.create@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        WorkOrderResponse order = createOrder(client, service);

        ThreadResponse res = threadService.create(createThreadRequest(client, order));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getWorkOrderId()).isEqualTo(order.getId());
    }

    @Test
    void create_byExpert() {
        var expert = createExpert("thread.expert.create2@sispro3d.com");
        var client = createClient("thread.client.create2@sispro3d.com");
        var service = createApprovedService("Texturizado PBR", expert);
        WorkOrderResponse order = createOrder(client, service);

        ThreadResponse res = threadService.create(createThreadRequest(expert, order));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getWorkOrderId()).isEqualTo(order.getId());
    }

    @Test
    void create_whenOrderDoesNotExist_throwsResourceNotFound() {
        var client = createClient("thread.client.noorder@sispro3d.com");

        var request = ThreadRequest.builder()
                .actorId(client.getIdUser())
                .workOrderId(999L)
                .build();

        assertThatThrownBy(() -> threadService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenAccountDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("thread.expert.noactor@sispro3d.com");
        var client = createClient("thread.client.noactor@sispro3d.com");
        var service = createApprovedService("Servicio sin actor", expert);
        WorkOrderResponse order = createOrder(client, service);

        var request = ThreadRequest.builder()
                .actorId(999L)
                .workOrderId(order.getId())
                .build();

        assertThatThrownBy(() -> threadService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenActorNotParticipant_throwsException() {
        var expert = createExpert("thread.expert.owner@sispro3d.com");
        var otherExpert = createExpert("thread.expert.other@sispro3d.com");
        var client = createClient("thread.client.owner@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        WorkOrderResponse order = createOrder(client, service);

        assertThatThrownBy(() -> threadService.create(createThreadRequest(otherExpert, order)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cliente o el experto");
    }

    @Test
    void create_whenThreadAlreadyExists_throwsException() {
        var expert = createExpert("thread.expert.duplicate@sispro3d.com");
        var client = createClient("thread.client.duplicate@sispro3d.com");
        var service = createApprovedService("Servicio con hilo", expert);
        WorkOrderResponse order = createOrder(client, service);
        threadService.create(createThreadRequest(client, order));

        assertThatThrownBy(() -> threadService.create(createThreadRequest(expert, order)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("un hilo de conversación por orden");
    }

    @Test
    void findById_whenExists() {
        var expert = createExpert("thread.expert.find@sispro3d.com");
        var client = createClient("thread.client.find@sispro3d.com");
        var service = createApprovedService("Animacion de personaje", expert);
        WorkOrderResponse order = createOrder(client, service);
        ThreadResponse created = threadService.create(createThreadRequest(client, order));

        Optional<ThreadResponse> res = threadService.findById(created.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getWorkOrderId()).isEqualTo(order.getId());
    }

    @Test
    void findById_whenNotExists() {
        Optional<ThreadResponse> res = threadService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var expert = createExpert("thread.expert.findAll@sispro3d.com");
        var client = createClient("thread.client.findAll@sispro3d.com");
        var service = createApprovedService("Rigging basico", expert);
        WorkOrderResponse order = createOrder(client, service);
        threadService.create(createThreadRequest(client, order));

        List<ThreadResponse> res = threadService.findAll();
        assertThat(res).isNotEmpty();
    }

    @Test
    void update_whenExists() {
        var expert = createExpert("thread.expert.update@sispro3d.com");
        var client = createClient("thread.client.update@sispro3d.com");
        var service = createApprovedService("Servicio actualizable", expert);
        WorkOrderResponse order = createOrder(client, service);
        ThreadResponse created = threadService.create(createThreadRequest(client, order));

        var updateRequest = ThreadRequest.builder()
                .actorId(client.getIdUser())
                .workOrderId(order.getId())
                .build();
        ThreadResponse updated = threadService.update(created.getId(), updateRequest);

        assertThat(updated.getWorkOrderId()).isEqualTo(order.getId());
    }

    @Test
    void update_whenNotExists() {
        var request = ThreadRequest.builder().build();

        assertThatThrownBy(() -> threadService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var expert = createExpert("thread.expert.delete@sispro3d.com");
        var client = createClient("thread.client.delete@sispro3d.com");
        var service = createApprovedService("Para borrar", expert);
        WorkOrderResponse order = createOrder(client, service);
        ThreadResponse created = threadService.create(createThreadRequest(client, order));

        threadService.delete(created.getId());

        assertThat(threadRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> threadService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var expert = createExpert("thread.expert.exists@sispro3d.com");
        var client = createClient("thread.client.exists@sispro3d.com");
        var service = createApprovedService("Servicio existente", expert);
        WorkOrderResponse order = createOrder(client, service);
        ThreadResponse created = threadService.create(createThreadRequest(client, order));

        assertThat(threadService.existsById(created.getId())).isTrue();
    }

    @Test
    void findByWorkOrderId() {
        var expert = createExpert("thread.expert.findByOrder@sispro3d.com");
        var client = createClient("thread.client.findByOrder@sispro3d.com");
        var service = createApprovedService("Servicio con hilo", expert);
        WorkOrderResponse order = createOrder(client, service);
        threadService.create(createThreadRequest(client, order));

        Optional<ThreadResponse> res = threadService.findByWorkOrderId(order.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getWorkOrderId()).isEqualTo(order.getId());
    }

    @Test
    void findByWorkOrderId_whenNoThread() {
        Optional<ThreadResponse> res = threadService.findByWorkOrderId(999L);
        assertThat(res).isEmpty();
    }
}
