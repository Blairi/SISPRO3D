package com.sispro3d.unam.core.storage;

import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.preview.domain.Preview;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoredFileAccessServiceTest {

    @Autowired
    private StoredFileAccessService storedFileAccessService;

    @Autowired
    private FileStorageService fileStorageService;

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

    @Autowired
    private DeliverableRepository deliverableRepository;

    @Autowired
    private PreviewRepository previewRepository;

    private Account client;
    private Account expert;
    private Account outsider;
    private Account otherClient;
    private String deliverableReference;
    private String previewReference;

    @BeforeEach
    void setUp() {
        client = createAccount("Client", "access.client@sispro3d.com", Role.CLIENT);
        expert = createAccount("Expert", "access.expert@sispro3d.com", Role.EXPERT);
        outsider = createAccount("Outsider", "access.outsider@sispro3d.com", Role.CLIENT);

        OfferedService service = createApprovedService("Acceso A", expert);
        Quote quote = createAcceptedQuote(client, service);
        WorkOrder order = createOrder(quote);

        deliverableReference = store("model.obj", "deliverables");
        previewReference = store("preview.png", "previews");
        createDeliverable(order, deliverableReference);
        createPreview(order, previewReference);

        Account otherExpert = createAccount("Other", "access.otherexpert@sispro3d.com", Role.EXPERT);
        otherClient = createAccount("OtherClient", "access.otherclient@sispro3d.com", Role.CLIENT);
        OfferedService otherService = createApprovedService("Acceso B", otherExpert);
        createOrder(createAcceptedQuote(otherClient, otherService));
    }

    @Test
    void clientOfTheOrderCanReadTheDeliverableFile() {
        Optional<Path> resolved = storedFileAccessService.resolveForUser(deliverableReference, client.getIdUser());

        assertThat(resolved).isPresent();
        assertThat(resolved.get().getFileName().toString()).endsWith(".obj");
    }

    @Test
    void expertOfTheOrderCanReadThePreviewFile() {
        Optional<Path> resolved = storedFileAccessService.resolveForUser(previewReference, expert.getIdUser());

        assertThat(resolved).isPresent();
        assertThat(resolved.get().getFileName().toString()).endsWith(".png");
    }

    @Test
    void outsiderCannotReadTheFile() {
        assertThat(storedFileAccessService.resolveForUser(deliverableReference, outsider.getIdUser())).isEmpty();
    }

    @Test
    void participantOfAnotherOrderCannotReadTheFile() {
        assertThat(storedFileAccessService.resolveForUser(deliverableReference, otherClient.getIdUser())).isEmpty();
    }

    @Test
    void unknownReferenceIsEmpty() {
        String unknown = "deliverables/" + UUID.randomUUID() + ".obj";

        assertThat(storedFileAccessService.resolveForUser(unknown, client.getIdUser())).isEmpty();
    }

    @Test
    void referenceOutsideTheStorageRootIsRejected() {
        assertThat(storedFileAccessService.resolveForUser("../secret.txt", client.getIdUser())).isEmpty();
    }

    @Test
    void externalHttpReferenceIsNotServed() {
        assertThat(storedFileAccessService.resolveForUser("https://files.render3d.mx/modelo.fbx", client.getIdUser()))
                .isEmpty();
    }

    private String store(String filename, String subfolder) {
        return fileStorageService.store(
                new MockMultipartFile("file", filename, "application/octet-stream", filename.getBytes()),
                subfolder);
    }

    private Account createAccount(String name, String email, Role role) {
        Account account = new Account();
        account.setName(name);
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword("test123");
        account.setRole(role);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private OfferedService createApprovedService(String title, Account serviceExpert) {
        Category category = new Category();
        category.setName("Cat " + title);
        category.setDescription("Test category");
        categoryRepository.save(category);

        OfferedService service = new OfferedService();
        service.setTitle(title);
        service.setDescription("Test description");
        service.setBasePrice(new BigDecimal("1500.00"));
        service.setExpert(serviceExpert);
        service.setCategory(category);
        service.setStatus(ServiceStatus.APPROVED);
        service.setDeliveryTimeDays(7);
        service.setCreatedAt(LocalDateTime.now());
        return offeredServiceRepository.save(service);
    }

    private Quote createAcceptedQuote(Account quoteClient, OfferedService service) {
        Quote quote = new Quote();
        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setTotalAmount(new BigDecimal("1500.00"));
        quote.setDescription("Test quote");
        quote.setClient(quoteClient);
        quote.setOfferedService(service);
        quote.setCreatedAt(LocalDateTime.now());
        return quoteRepository.save(quote);
    }

    private WorkOrder createOrder(Quote quote) {
        WorkOrder order = new WorkOrder();
        order.setStatus(WorkOrderStatus.IN_PROGRESS);
        order.setQuote(quote);
        order.setCreatedAt(LocalDateTime.now());
        return workOrderRepository.save(order);
    }

    private void createDeliverable(WorkOrder order, String urlFile) {
        Deliverable deliverable = new Deliverable();
        deliverable.setName("Modelo OBJ");
        deliverable.setUrlFile(urlFile);
        deliverable.setFileType("model/obj");
        deliverable.setCreatedAt(LocalDateTime.now());
        deliverable.setWorkOrder(order);
        deliverableRepository.save(deliverable);
    }

    private void createPreview(WorkOrder order, String urlFile) {
        Deliverable deliverable = deliverableRepository.findByWorkOrder_Id(order.getId()).get(0);
        Preview preview = new Preview();
        preview.setCaption("Vista frontal");
        preview.setUrlFile(urlFile);
        preview.setDeliverable(deliverable);
        preview.setCreatedAt(LocalDateTime.now());
        previewRepository.save(preview);
    }
}
