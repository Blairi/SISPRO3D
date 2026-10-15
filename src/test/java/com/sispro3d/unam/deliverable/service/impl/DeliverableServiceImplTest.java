package com.sispro3d.unam.deliverable.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.core.storage.FileStorageService;
import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.preview.dto.PreviewRequest;
import com.sispro3d.unam.preview.dto.PreviewResponse;
import com.sispro3d.unam.preview.service.PreviewService;
import com.sispro3d.unam.quote.dto.QuoteAcceptance;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeliverableServiceImplTest {

    @TempDir
    static Path uploadsDir;

    @DynamicPropertySource
    static void storageLocation(DynamicPropertyRegistry registry) {
        registry.add("app.storage.location", () -> uploadsDir.toString());
    }

    @Autowired
    private DeliverableService deliverableService;

    @Autowired
    private DeliverableRepository deliverableRepository;

    @Autowired
    private FileStorageService fileStorageService;

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

    @Autowired
    private PreviewService previewService;

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

    private QuoteAcceptance createAcceptedQuote(Account client, OfferedService service) {
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
        WorkOrderResponse created = createAcceptedQuote(client, service).workOrder();
        return workOrderService.start(created.getId(), expert.getIdUser());
    }

    private MockMultipartFile modelFile(String filename) {
        return new MockMultipartFile("file", filename, "application/octet-stream", "model-bytes".getBytes());
    }

    private MockMultipartFile imageFile(String filename) {
        return new MockMultipartFile("file", filename, "image/png", "image-bytes".getBytes());
    }

    private DeliverableRequest createDeliverableRequest(Account expert, WorkOrderResponse order) {
        return DeliverableRequest.builder()
                .expertId(expert.getIdUser())
                .workOrderId(order.getId())
                .name("Modelo FBX")
                .file(modelFile("modelo.fbx"))
                .build();
    }

    @Test
    void create() {
        var expert = createExpert("deliverable.expert.create@sispro3d.com");
        var client = createClient("deliverable.client.create@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);

        DeliverableResponse res = deliverableService.create(createDeliverableRequest(expert, order));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getName()).isEqualTo("Modelo FBX");
        assertThat(res.getUrlFile()).startsWith("deliverables/").endsWith(".fbx");
        assertThat(res.getFileType()).isEqualTo("FBX");
        assertThat(res.getWorkOrderId()).isEqualTo(order.getId());
        assertThat(res.getCreatedAt()).isNotNull();
        assertThat(Files.exists(fileStorageService.resolve(res.getUrlFile()))).isTrue();
    }

    @Test
    void create_whenFileTypeNotAllowed_throwsBadRequest_andStoresNothing() {
        var expert = createExpert("deliverable.expert.badtype@sispro3d.com");
        var client = createClient("deliverable.client.badtype@sispro3d.com");
        var service = createApprovedService("Tipo invalido", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);

        var request = DeliverableRequest.builder()
                .expertId(expert.getIdUser())
                .workOrderId(order.getId())
                .name("Mal tipo")
                .file(modelFile("modelo.exe"))
                .build();

        assertThatThrownBy(() -> deliverableService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Formato no permitido");
        assertThat(deliverableService.findByWorkOrderId(order.getId())).isEmpty();
    }

    @Test
    void create_whenOrderDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("deliverable.expert.noorder@sispro3d.com");

        var request = DeliverableRequest.builder()
                .expertId(expert.getIdUser())
                .workOrderId(999L)
                .name("Modelo")
                .file(modelFile("modelo.fbx"))
                .build();

        assertThatThrownBy(() -> deliverableService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenExpertDoesNotOwnOrder_throwsException() {
        var expert = createExpert("deliverable.expert.owner@sispro3d.com");
        var otherExpert = createExpert("deliverable.expert.other@sispro3d.com");
        var client = createClient("deliverable.client.owner@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);

        assertThatThrownBy(() -> deliverableService.create(createDeliverableRequest(otherExpert, order)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("propietario");
    }

    @Test
    void create_whenAccountIsNotExpert_throwsException() {
        var expert = createExpert("deliverable.expert.notexpert@sispro3d.com");
        var client = createClient("deliverable.client.notexpert@sispro3d.com");
        var service = createApprovedService("Servicio con experto", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);

        assertThatThrownBy(() -> deliverableService.create(createDeliverableRequest(client, order)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("EXPERT");
    }

    @Test
    void create_whenOrderNotActive_throwsException() {
        var expert = createExpert("deliverable.expert.pending@sispro3d.com");
        var client = createClient("deliverable.client.pending@sispro3d.com");
        var service = createApprovedService("Servicio pendiente", expert);
        WorkOrderResponse order = createAcceptedQuote(client, service).workOrder();

        assertThatThrownBy(() -> deliverableService.create(createDeliverableRequest(expert, order)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("IN_PROGRESS o IN_REVIEW");
    }

    @Test
    void findById_whenExists() {
        var expert = createExpert("deliverable.expert.find@sispro3d.com");
        var client = createClient("deliverable.client.find@sispro3d.com");
        var service = createApprovedService("Texturizado PBR", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse created = deliverableService.create(createDeliverableRequest(expert, order));

        Optional<DeliverableResponse> res = deliverableService.findById(created.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getName()).isEqualTo("Modelo FBX");
    }

    @Test
    void findById_whenNotExists() {
        Optional<DeliverableResponse> res = deliverableService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var expert = createExpert("deliverable.expert.findAll@sispro3d.com");
        var client = createClient("deliverable.client.findAll@sispro3d.com");
        var service = createApprovedService("Animacion de personaje", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        deliverableService.create(createDeliverableRequest(expert, order));

        List<DeliverableResponse> res = deliverableService.findAll();
        assertThat(res).isNotEmpty();
    }

    @Test
    void update_whenExists() {
        var expert = createExpert("deliverable.expert.update@sispro3d.com");
        var client = createClient("deliverable.client.update@sispro3d.com");
        var service = createApprovedService("Rigging basico", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse created = deliverableService.create(createDeliverableRequest(expert, order));
        Path previous = fileStorageService.resolve(created.getUrlFile());

        var updateRequest = DeliverableRequest.builder()
                .expertId(expert.getIdUser())
                .workOrderId(order.getId())
                .name("Modelo OBJ")
                .file(modelFile("modelo.obj"))
                .build();
        DeliverableResponse updated = deliverableService.update(created.getId(), updateRequest);

        assertThat(updated.getName()).isEqualTo("Modelo OBJ");
        assertThat(updated.getFileType()).isEqualTo("OBJ");
        assertThat(updated.getUrlFile()).startsWith("deliverables/").endsWith(".obj");
        assertThat(Files.exists(previous)).isFalse();
        assertThat(Files.exists(fileStorageService.resolve(updated.getUrlFile()))).isTrue();
    }

    @Test
    void update_whenNotExists() {
        var request = DeliverableRequest.builder()
                .name("No existe")
                .build();

        assertThatThrownBy(() -> deliverableService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var expert = createExpert("deliverable.expert.delete@sispro3d.com");
        var client = createClient("deliverable.client.delete@sispro3d.com");
        var service = createApprovedService("Para borrar", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse created = deliverableService.create(createDeliverableRequest(expert, order));
        Path stored = fileStorageService.resolve(created.getUrlFile());

        deliverableService.delete(created.getId());

        assertThat(deliverableRepository.existsById(created.getId())).isFalse();
        assertThat(Files.exists(stored)).isFalse();
    }

    @Test
    void delete_removesDeliverableAndItsPreviewFiles() {
        var expert = createExpert("deliverable.expert.cascade@sispro3d.com");
        var client = createClient("deliverable.client.cascade@sispro3d.com");
        var service = createApprovedService("Cascada", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse deliverable = deliverableService.create(createDeliverableRequest(expert, order));
        PreviewResponse preview = previewService.create(PreviewRequest.builder()
                .expertId(expert.getIdUser())
                .deliverableId(deliverable.getId())
                .caption("Vista frontal")
                .file(imageFile("preview.png"))
                .build());
        Path deliverableFile = fileStorageService.resolve(deliverable.getUrlFile());
        Path previewFile = fileStorageService.resolve(preview.getUrlFile());

        deliverableService.delete(deliverable.getId());

        assertThat(deliverableRepository.existsById(deliverable.getId())).isFalse();
        assertThat(Files.exists(deliverableFile)).isFalse();
        assertThat(Files.exists(previewFile)).isFalse();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> deliverableService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var expert = createExpert("deliverable.expert.exists@sispro3d.com");
        var client = createClient("deliverable.client.exists@sispro3d.com");
        var service = createApprovedService("Servicio existente", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        DeliverableResponse created = deliverableService.create(createDeliverableRequest(expert, order));

        assertThat(deliverableService.existsById(created.getId())).isTrue();
    }

    @Test
    void findByWorkOrderId() {
        var expert = createExpert("deliverable.expert.findByOrder@sispro3d.com");
        var client = createClient("deliverable.client.findByOrder@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        WorkOrderResponse order = createInProgressOrder(client, service, expert);
        deliverableService.create(createDeliverableRequest(expert, order));

        List<DeliverableResponse> res = deliverableService.findByWorkOrderId(order.getId());

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getWorkOrderId()).isEqualTo(order.getId());
    }

    @Test
    void findByWorkOrderId_whenNoDeliverables() {
        List<DeliverableResponse> res = deliverableService.findByWorkOrderId(999L);
        assertThat(res).isEmpty();
    }
}
