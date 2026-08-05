package com.sispro3d.unam.preview.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.preview.dto.PreviewRequest;
import com.sispro3d.unam.preview.dto.PreviewResponse;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.preview.service.PreviewService;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
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
class PreviewServiceImplTest {

    @Autowired
    private PreviewService previewService;

    @Autowired
    private PreviewRepository previewRepository;

    @Autowired
    private DeliverableService deliverableService;

    @Autowired
    private DeliverableRepository deliverableRepository;

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

    private WorkOrderResponse createInProgressOrder(Account client, OfferedService service, Account expert) {
        QuoteResponse quote = createAcceptedQuote(client, service);
        var request = WorkOrderRequest.builder()
                .clientId(client.getIdUser())
                .quoteId(quote.getId())
                .build();
        WorkOrderResponse created = workOrderService.create(request);
        return workOrderService.start(created.getId(), expert.getIdUser());
    }

    private DeliverableResponse createDeliverable(Account expert, WorkOrderResponse order) {
        var request = DeliverableRequest.builder()
                .expertId(expert.getIdUser())
                .workOrderId(order.getId())
                .name("Modelo FBX")
                .urlFile("https://files.render3d.mx/modelo.fbx")
                .fileType("model/fbx")
                .build();
        return deliverableService.create(request);
    }

    private PreviewRequest createPreviewRequest(Account expert, DeliverableResponse deliverable) {
        return PreviewRequest.builder()
                .expertId(expert.getIdUser())
                .deliverableId(deliverable.getId())
                .caption("Vista frontal del personaje")
                .urlFile("https://files.render3d.mx/preview-frente.png")
                .build();
    }

    @Test
    void create() {
        var expert = createExpert("preview.expert.create@sispro3d.com");
        var client = createClient("preview.client.create@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = createDeliverable(expert, order);

        PreviewResponse res = previewService.create(createPreviewRequest(expert, deliverable));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getCaption()).isEqualTo("Vista frontal del personaje");
        assertThat(res.getUrlFile()).isEqualTo("https://files.render3d.mx/preview-frente.png");
        assertThat(res.getDeliverableId()).isEqualTo(deliverable.getId());
    }

    @Test
    void create_whenDeliverableDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("preview.expert.nodeliverable@sispro3d.com");

        var request = PreviewRequest.builder()
                .expertId(expert.getIdUser())
                .deliverableId(999L)
                .caption("Vista frontal")
                .urlFile("https://files.render3d.mx/preview.png")
                .build();

        assertThatThrownBy(() -> previewService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenExpertDoesNotOwnOrder_throwsException() {
        var expert = createExpert("preview.expert.owner@sispro3d.com");
        var otherExpert = createExpert("preview.expert.other@sispro3d.com");
        var client = createClient("preview.client.owner@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = createDeliverable(expert, order);

        assertThatThrownBy(() -> previewService.create(createPreviewRequest(otherExpert, deliverable)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("propietario");
    }

    @Test
    void create_whenAccountIsNotExpert_throwsException() {
        var expert = createExpert("preview.expert.notexpert@sispro3d.com");
        var client = createClient("preview.client.notexpert@sispro3d.com");
        var service = createApprovedService("Servicio con experto", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = createDeliverable(expert, order);

        assertThatThrownBy(() -> previewService.create(createPreviewRequest(client, deliverable)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EXPERT");
    }

    @Test
    void create_whenOrderNotActive_throwsException() {
        var expert = createExpert("preview.expert.pending@sispro3d.com");
        var client = createClient("preview.client.pending@sispro3d.com");
        var service = createApprovedService("Servicio pendiente", expert);
        QuoteResponse quote = createAcceptedQuote(client, service);

        var orderRequest = WorkOrderRequest.builder()
                .clientId(client.getIdUser())
                .quoteId(quote.getId())
                .build();
        WorkOrderResponse order = workOrderService.create(orderRequest);

        WorkOrder orderEntity = workOrderRepository.findById(order.getId()).orElseThrow();
        var deliverable = new Deliverable();
        deliverable.setName("Modelo FBX");
        deliverable.setUrlFile("https://files.render3d.mx/modelo.fbx");
        deliverable.setFileType("model/fbx");
        deliverable.setCreatedAt(LocalDateTime.now());
        deliverable.setWorkOrder(orderEntity);
        deliverableRepository.save(deliverable);

        var request = PreviewRequest.builder()
                .expertId(expert.getIdUser())
                .deliverableId(deliverable.getId())
                .caption("Vista frontal")
                .urlFile("https://files.render3d.mx/preview.png")
                .build();

        assertThatThrownBy(() -> previewService.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("IN_PROGRESS o IN_REVIEW");
    }

    @Test
    void findById_whenExists() {
        var expert = createExpert("preview.expert.find@sispro3d.com");
        var client = createClient("preview.client.find@sispro3d.com");
        var service = createApprovedService("Texturizado PBR", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = createDeliverable(expert, order);
        PreviewResponse created = previewService.create(createPreviewRequest(expert, deliverable));

        Optional<PreviewResponse> res = previewService.findById(created.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getCaption()).isEqualTo("Vista frontal del personaje");
    }

    @Test
    void findById_whenNotExists() {
        Optional<PreviewResponse> res = previewService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var expert = createExpert("preview.expert.findAll@sispro3d.com");
        var client = createClient("preview.client.findAll@sispro3d.com");
        var service = createApprovedService("Animacion de personaje", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = createDeliverable(expert, order);
        previewService.create(createPreviewRequest(expert, deliverable));

        List<PreviewResponse> res = previewService.findAll();
        assertThat(res).isNotEmpty();
    }

    @Test
    void update_whenExists() {
        var expert = createExpert("preview.expert.update@sispro3d.com");
        var client = createClient("preview.client.update@sispro3d.com");
        var service = createApprovedService("Rigging basico", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = createDeliverable(expert, order);
        PreviewResponse created = previewService.create(createPreviewRequest(expert, deliverable));

        var updateRequest = PreviewRequest.builder()
                .expertId(expert.getIdUser())
                .deliverableId(deliverable.getId())
                .caption("Vista lateral del personaje")
                .urlFile("https://files.render3d.mx/preview-lateral.png")
                .build();
        PreviewResponse updated = previewService.update(created.getId(), updateRequest);

        assertThat(updated.getCaption()).isEqualTo("Vista lateral del personaje");
        assertThat(updated.getUrlFile()).isEqualTo("https://files.render3d.mx/preview-lateral.png");
    }

    @Test
    void update_whenNotExists() {
        var request = PreviewRequest.builder()
                .caption("No existe")
                .build();

        assertThatThrownBy(() -> previewService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var expert = createExpert("preview.expert.delete@sispro3d.com");
        var client = createClient("preview.client.delete@sispro3d.com");
        var service = createApprovedService("Para borrar", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = createDeliverable(expert, order);
        PreviewResponse created = previewService.create(createPreviewRequest(expert, deliverable));

        previewService.delete(created.getId());

        assertThat(previewRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> previewService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var expert = createExpert("preview.expert.exists@sispro3d.com");
        var client = createClient("preview.client.exists@sispro3d.com");
        var service = createApprovedService("Servicio existente", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = createDeliverable(expert, order);
        PreviewResponse created = previewService.create(createPreviewRequest(expert, deliverable));

        assertThat(previewService.existsById(created.getId())).isTrue();
    }

    @Test
    void findByDeliverableId() {
        var expert = createExpert("preview.expert.findByDeliverable@sispro3d.com");
        var client = createClient("preview.client.findByDeliverable@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = createDeliverable(expert, order);
        previewService.create(createPreviewRequest(expert, deliverable));

        List<PreviewResponse> res = previewService.findByDeliverableId(deliverable.getId());

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getDeliverableId()).isEqualTo(deliverable.getId());
    }

    @Test
    void findByDeliverableId_whenNoPreviews() {
        List<PreviewResponse> res = previewService.findByDeliverableId(999L);
        assertThat(res).isEmpty();
    }
}
