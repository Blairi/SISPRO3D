package com.sispro3d.unam.workorder.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
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
class WorkOrderServiceImplTest {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

    @Autowired
    private QuoteService quoteService;

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

    private QuoteResponse createAcceptedQuote(Account client, OfferedService service) {
        var request = QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Solicito cotización para mi proyecto")
                .totalAmount(new BigDecimal("1500.00"))
                .build();
        QuoteResponse created = quoteService.create(request);
        return quoteService.accept(created.getId(), client.getIdUser());
    }

    private WorkOrderResponse createOrder(Account client, OfferedService service) {
        QuoteResponse quote = createAcceptedQuote(client, service);
        var request = WorkOrderRequest.builder()
                .clientId(client.getIdUser())
                .quoteId(quote.getId())
                .build();
        return workOrderService.create(request);
    }

    @Test
    void create() {
        var expert = createExpert("workorder.expert.create@sispro3d.com");
        var client = createClient("workorder.client.create@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);

        WorkOrderResponse res = createOrder(client, service);

        assertThat(res.getId()).isNotNull();
        assertThat(res.getStatus()).isEqualTo(WorkOrderStatus.PENDING);
        assertThat(res.getClientId()).isEqualTo(client.getIdUser());
        assertThat(res.getOfferedServiceId()).isEqualTo(service.getId());
        assertThat(res.getCreatedAt()).isNotNull();
    }

    @Test
    void create_whenClientDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("workorder.expert.noclient@sispro3d.com");
        var client = createClient("workorder.client.noclient@sispro3d.com");
        var service = createApprovedService("Servicio sin cliente", expert);
        QuoteResponse quote = createAcceptedQuote(client, service);

        var request = WorkOrderRequest.builder()
                .clientId(999L)
                .quoteId(quote.getId())
                .build();

        assertThatThrownBy(() -> workOrderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenQuoteDoesNotExist_throwsResourceNotFound() {
        var client = createClient("workorder.client.noquote@sispro3d.com");

        var request = WorkOrderRequest.builder()
                .clientId(client.getIdUser())
                .quoteId(999L)
                .build();

        assertThatThrownBy(() -> workOrderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenAccountIsNotClient_throwsException() {
        var expert = createExpert("workorder.expert.notclient@sispro3d.com");
        var client = createClient("workorder.client.notclient@sispro3d.com");
        var service = createApprovedService("Servicio con experto", expert);
        QuoteResponse quote = createAcceptedQuote(client, service);

        var request = WorkOrderRequest.builder()
                .clientId(expert.getIdUser())
                .quoteId(quote.getId())
                .build();

        assertThatThrownBy(() -> workOrderService.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLIENT");
    }

    @Test
    void create_whenQuoteNotAccepted_throwsException() {
        var expert = createExpert("workorder.expert.pending@sispro3d.com");
        var client = createClient("workorder.client.pending@sispro3d.com");
        var service = createApprovedService("Servicio pendiente", expert);

        var request = QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Solicito cotización")
                .totalAmount(new BigDecimal("1500.00"))
                .build();
        QuoteResponse pending = quoteService.create(request);

        var orderRequest = WorkOrderRequest.builder()
                .clientId(client.getIdUser())
                .quoteId(pending.getId())
                .build();

        assertThatThrownBy(() -> workOrderService.create(orderRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACCEPTED");
    }

    @Test
    void create_whenClientDoesNotOwnQuote_throwsException() {
        var expert = createExpert("workorder.expert.owner@sispro3d.com");
        var client = createClient("workorder.client.owner@sispro3d.com");
        var otherClient = createClient("workorder.client.other@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        QuoteResponse quote = createAcceptedQuote(client, service);

        var request = WorkOrderRequest.builder()
                .clientId(otherClient.getIdUser())
                .quoteId(quote.getId())
                .build();

        assertThatThrownBy(() -> workOrderService.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("propietario");
    }

    @Test
    void findById_whenExists() {
        var expert = createExpert("workorder.expert.find@sispro3d.com");
        var client = createClient("workorder.client.find@sispro3d.com");
        var service = createApprovedService("Texturizado PBR", expert);
        WorkOrderResponse created = createOrder(client, service);

        Optional<WorkOrderResponse> res = workOrderService.findById(created.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getStatus()).isEqualTo(WorkOrderStatus.PENDING);
    }

    @Test
    void findById_whenNotExists() {
        Optional<WorkOrderResponse> res = workOrderService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var expert = createExpert("workorder.expert.findAll@sispro3d.com");
        var client = createClient("workorder.client.findAll@sispro3d.com");
        var service = createApprovedService("Animacion de personaje", expert);
        createOrder(client, service);

        List<WorkOrderResponse> res = workOrderService.findAll();
        assertThat(res).isNotEmpty();
    }

    @Test
    void update_whenExists() {
        var expert = createExpert("workorder.expert.update@sispro3d.com");
        var client = createClient("workorder.client.update@sispro3d.com");
        var service = createApprovedService("Rigging basico", expert);
        WorkOrderResponse created = createOrder(client, service);

        QuoteResponse secondQuote = createAcceptedQuote(client, service);
        var updateRequest = WorkOrderRequest.builder()
                .clientId(client.getIdUser())
                .quoteId(secondQuote.getId())
                .build();
        WorkOrderResponse updated = workOrderService.update(created.getId(), updateRequest);

        assertThat(updated.getQuoteId()).isEqualTo(secondQuote.getId());
        assertThat(updated.getStatus()).isEqualTo(WorkOrderStatus.PENDING);
    }

    @Test
    void update_whenNotExists() {
        var request = WorkOrderRequest.builder().build();

        assertThatThrownBy(() -> workOrderService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var expert = createExpert("workorder.expert.delete@sispro3d.com");
        var client = createClient("workorder.client.delete@sispro3d.com");
        var service = createApprovedService("Para borrar", expert);
        WorkOrderResponse created = createOrder(client, service);

        workOrderService.delete(created.getId());

        assertThat(workOrderRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> workOrderService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var expert = createExpert("workorder.expert.exists@sispro3d.com");
        var client = createClient("workorder.client.exists@sispro3d.com");
        var service = createApprovedService("Servicio existente", expert);
        WorkOrderResponse created = createOrder(client, service);

        assertThat(workOrderService.existsById(created.getId())).isTrue();
    }

    @Test
    void start() {
        var expert = createExpert("workorder.expert.start@sispro3d.com");
        var client = createClient("workorder.client.start@sispro3d.com");
        var service = createApprovedService("Modelado estilizado", expert);
        WorkOrderResponse created = createOrder(client, service);

        WorkOrderResponse started = workOrderService.start(created.getId(), expert.getIdUser());

        assertThat(started.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
        assertThat(started.getStartedAt()).isNotNull();
    }

    @Test
    void start_whenExpertDoesNotOwnService_throwsException() {
        var expert = createExpert("workorder.expert.owner@sispro3d.com");
        var otherExpert = createExpert("workorder.expert.other@sispro3d.com");
        var client = createClient("workorder.client.start@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        WorkOrderResponse created = createOrder(client, service);

        assertThatThrownBy(() -> workOrderService.start(created.getId(), otherExpert.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("propietario");
    }

    @Test
    void start_whenAccountIsNotExpert_throwsException() {
        var expert = createExpert("workorder.expert.start2@sispro3d.com");
        var client = createClient("workorder.client.start2@sispro3d.com");
        var service = createApprovedService("Servicio 2", expert);
        WorkOrderResponse created = createOrder(client, service);

        assertThatThrownBy(() -> workOrderService.start(created.getId(), client.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EXPERT");
    }

    @Test
    void start_whenNotPending_throwsException() {
        var expert = createExpert("workorder.expert.start3@sispro3d.com");
        var client = createClient("workorder.client.start3@sispro3d.com");
        var service = createApprovedService("Servicio 3", expert);
        WorkOrderResponse created = createOrder(client, service);
        workOrderService.start(created.getId(), expert.getIdUser());

        assertThatThrownBy(() -> workOrderService.start(created.getId(), expert.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void start_whenOrderDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("workorder.expert.start4@sispro3d.com");

        assertThatThrownBy(() -> workOrderService.start(999L, expert.getIdUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void start_whenExpertDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("workorder.expert.start5@sispro3d.com");
        var client = createClient("workorder.client.start5@sispro3d.com");
        var service = createApprovedService("Servicio 5", expert);
        WorkOrderResponse created = createOrder(client, service);

        assertThatThrownBy(() -> workOrderService.start(created.getId(), 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markInReview() {
        var expert = createExpert("workorder.expert.review@sispro3d.com");
        var client = createClient("workorder.client.review@sispro3d.com");
        var service = createApprovedService("Servicio en revisión", expert);
        WorkOrderResponse created = createOrder(client, service);
        workOrderService.start(created.getId(), expert.getIdUser());

        WorkOrderResponse reviewed = workOrderService.markInReview(created.getId(), expert.getIdUser());

        assertThat(reviewed.getStatus()).isEqualTo(WorkOrderStatus.IN_REVIEW);
    }

    @Test
    void markInReview_whenNotInProgress_throwsException() {
        var expert = createExpert("workorder.expert.review2@sispro3d.com");
        var client = createClient("workorder.client.review2@sispro3d.com");
        var service = createApprovedService("Servicio en revisión 2", expert);
        WorkOrderResponse created = createOrder(client, service);

        assertThatThrownBy(() -> workOrderService.markInReview(created.getId(), expert.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("IN_PROGRESS");
    }

    @Test
    void requestChanges() {
        var expert = createExpert("workorder.expert.changes@sispro3d.com");
        var client = createClient("workorder.client.changes@sispro3d.com");
        var service = createApprovedService("Servicio con cambios", expert);
        WorkOrderResponse created = createOrder(client, service);
        workOrderService.start(created.getId(), expert.getIdUser());
        workOrderService.markInReview(created.getId(), expert.getIdUser());

        WorkOrderResponse changed = workOrderService.requestChanges(created.getId(), client.getIdUser());

        assertThat(changed.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
    }

    @Test
    void requestChanges_whenNotInReview_throwsException() {
        var expert = createExpert("workorder.expert.changes2@sispro3d.com");
        var client = createClient("workorder.client.changes2@sispro3d.com");
        var service = createApprovedService("Servicio con cambios 2", expert);
        WorkOrderResponse created = createOrder(client, service);
        workOrderService.start(created.getId(), expert.getIdUser());

        assertThatThrownBy(() -> workOrderService.requestChanges(created.getId(), client.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("IN_REVIEW");
    }

    @Test
    void complete() {
        var expert = createExpert("workorder.expert.complete@sispro3d.com");
        var client = createClient("workorder.client.complete@sispro3d.com");
        var service = createApprovedService("Servicio a completar", expert);
        WorkOrderResponse created = createOrder(client, service);
        workOrderService.start(created.getId(), expert.getIdUser());
        workOrderService.markInReview(created.getId(), expert.getIdUser());

        WorkOrderResponse completed = workOrderService.complete(created.getId(), client.getIdUser());

        assertThat(completed.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
    }

    @Test
    void complete_whenNotInReview_throwsException() {
        var expert = createExpert("workorder.expert.complete2@sispro3d.com");
        var client = createClient("workorder.client.complete2@sispro3d.com");
        var service = createApprovedService("Servicio a completar 2", expert);
        WorkOrderResponse created = createOrder(client, service);

        assertThatThrownBy(() -> workOrderService.complete(created.getId(), client.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("IN_REVIEW");
    }

    @Test
    void complete_whenClientDoesNotOwnOrder_throwsException() {
        var expert = createExpert("workorder.expert.complete3@sispro3d.com");
        var client = createClient("workorder.client.complete3@sispro3d.com");
        var otherClient = createClient("workorder.client.complete4@sispro3d.com");
        var service = createApprovedService("Servicio a completar 3", expert);
        WorkOrderResponse created = createOrder(client, service);
        workOrderService.start(created.getId(), expert.getIdUser());
        workOrderService.markInReview(created.getId(), expert.getIdUser());

        assertThatThrownBy(() -> workOrderService.complete(created.getId(), otherClient.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("propietario");
    }

    @Test
    void cancel() {
        var expert = createExpert("workorder.expert.cancel@sispro3d.com");
        var client = createClient("workorder.client.cancel@sispro3d.com");
        var service = createApprovedService("Servicio a cancelar", expert);
        WorkOrderResponse created = createOrder(client, service);

        WorkOrderResponse canceled = workOrderService.cancel(created.getId(), client.getIdUser());

        assertThat(canceled.getStatus()).isEqualTo(WorkOrderStatus.CANCELED);
    }

    @Test
    void cancel_whenInReview_throwsException() {
        var expert = createExpert("workorder.expert.cancel2@sispro3d.com");
        var client = createClient("workorder.client.cancel2@sispro3d.com");
        var service = createApprovedService("Servicio a cancelar 2", expert);
        WorkOrderResponse created = createOrder(client, service);
        workOrderService.start(created.getId(), expert.getIdUser());
        workOrderService.markInReview(created.getId(), expert.getIdUser());

        assertThatThrownBy(() -> workOrderService.cancel(created.getId(), client.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING o IN_PROGRESS");
    }

    @Test
    void findByQuoteId() {
        var expert = createExpert("workorder.expert.findByQuote@sispro3d.com");
        var client = createClient("workorder.client.findByQuote@sispro3d.com");
        var service = createApprovedService("Servicio cotizado", expert);
        QuoteResponse quote = createAcceptedQuote(client, service);

        var request = WorkOrderRequest.builder()
                .clientId(client.getIdUser())
                .quoteId(quote.getId())
                .build();
        workOrderService.create(request);

        Optional<WorkOrderResponse> res = workOrderService.findByQuoteId(quote.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getQuoteId()).isEqualTo(quote.getId());
    }

    @Test
    void findByClientId() {
        var expert = createExpert("workorder.expert.findByClient@sispro3d.com");
        var client = createClient("workorder.client.findByClient@sispro3d.com");
        var service = createApprovedService("Servicio del cliente", expert);
        createOrder(client, service);

        List<WorkOrderResponse> res = workOrderService.findByClientId(client.getIdUser());

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getClientId()).isEqualTo(client.getIdUser());
    }

    @Test
    void findByClientId_whenIdDoesNotExist() {
        List<WorkOrderResponse> res = workOrderService.findByClientId(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findByExpertId() {
        var expert = createExpert("workorder.expert.findByExpert@sispro3d.com");
        var client = createClient("workorder.client.findByExpert@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        createOrder(client, service);

        List<WorkOrderResponse> res = workOrderService.findByExpertId(expert.getIdUser());

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getOfferedServiceId()).isEqualTo(service.getId());
    }

    @Test
    void findByExpertId_whenNoOrders() {
        var expert = createExpert("workorder.expert.empty@sispro3d.com");

        List<WorkOrderResponse> res = workOrderService.findByExpertId(expert.getIdUser());

        assertThat(res).isEmpty();
    }

    @Test
    void findByStatus() {
        var expert = createExpert("workorder.expert.findByStatus@sispro3d.com");
        var client = createClient("workorder.client.findByStatus@sispro3d.com");
        var service = createApprovedService("Servicio pendiente", expert);
        createOrder(client, service);

        List<WorkOrderResponse> pending = workOrderService.findByStatus(WorkOrderStatus.PENDING);
        List<WorkOrderResponse> completed = workOrderService.findByStatus(WorkOrderStatus.COMPLETED);

        assertThat(pending).isNotEmpty();
        assertThat(pending).allMatch(o -> o.getStatus() == WorkOrderStatus.PENDING);
        assertThat(completed).isEmpty();
    }
}
