package com.sispro3d.unam.core.storage;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
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
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    private static final String PASSWORD = "test123";
    private static final byte[] DELIVERABLE_BYTES = "model-bytes".getBytes();
    private static final byte[] PREVIEW_BYTES = "png-bytes".getBytes();

    private Account client;
    private Account expert;
    private Account admin;
    private Account outsider;
    private Account otherClient;
    private String deliverableReference;
    private String previewReference;

    @BeforeEach
    void setUp() {
        client = createAccount("Client", "file.client@sispro3d.com", Role.CLIENT);
        expert = createAccount("Expert", "file.expert@sispro3d.com", Role.EXPERT);
        admin = createAccount("Admin", "file.admin@sispro3d.com", Role.ADMIN);
        outsider = createAccount("Outsider", "file.outsider@sispro3d.com", Role.CLIENT);

        OfferedService service = createApprovedService("Archivos A", expert);
        Quote quote = createAcceptedQuote(client, service);
        WorkOrder order = createOrder(quote);

        deliverableReference = store("model.obj", "deliverables", DELIVERABLE_BYTES);
        previewReference = store("preview.png", "previews", PREVIEW_BYTES);
        Deliverable deliverable = createDeliverable(order, deliverableReference);
        createPreview(deliverable, previewReference);

        Account otherExpert = createAccount("Other", "file.otherexpert@sispro3d.com", Role.EXPERT);
        otherClient = createAccount("OtherClient", "file.otherclient@sispro3d.com", Role.CLIENT);
        OfferedService otherService = createApprovedService("Archivos B", otherExpert);
        createOrder(createAcceptedQuote(otherClient, otherService));
    }

    @Test
    void clientOfTheOrderCanReadTheDeliverable() throws Exception {
        mockMvc.perform(get("/files/" + deliverableReference).session(login(client.getEmail())))
                .andExpect(status().isOk())
                .andExpect(content().bytes(DELIVERABLE_BYTES));
    }

    @Test
    void expertOfTheOrderCanReadThePreview() throws Exception {
        mockMvc.perform(get("/files/" + previewReference).session(login(expert.getEmail())))
                .andExpect(status().isOk())
                .andExpect(content().bytes(PREVIEW_BYTES));
    }

    @Test
    void outsiderIsNotFound() throws Exception {
        mockMvc.perform(get("/files/" + deliverableReference).session(login(outsider.getEmail())))
                .andExpect(status().isNotFound())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    void administratorIsNotFound() throws Exception {
        mockMvc.perform(get("/files/" + deliverableReference).session(login(admin.getEmail())))
                .andExpect(status().isNotFound())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    void participantOfAnotherOrderIsNotFound() throws Exception {
        mockMvc.perform(get("/files/" + deliverableReference).session(login(otherClient.getEmail())))
                .andExpect(status().isNotFound())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    void unknownReferenceIsNotFound() throws Exception {
        String unknown = "deliverables/" + UUID.randomUUID() + ".obj";

        mockMvc.perform(get("/files/" + unknown).session(login(client.getEmail())))
                .andExpect(status().isNotFound())
                .andExpect(content().bytes(new byte[0]));
    }

    @Test
    void unauthenticatedRequestDoesNotReceiveBytes() throws Exception {
        mockMvc.perform(get("/files/" + deliverableReference))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void traversalReferenceDoesNotServeBytes() throws Exception {
        mockMvc.perform(get("/files/..%2f..%2fsecret.txt").session(login(client.getEmail())))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("model-bytes"))));
    }

    private String store(String filename, String subfolder, byte[] content) {
        return fileStorageService.store(
                new MockMultipartFile("file", filename, "application/octet-stream", content), subfolder);
    }

    private MockHttpSession login(String email) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", email)
                        .param("password", PASSWORD))
                .andExpect(status().isFound())
                .andReturn().getRequest().getSession(false);
    }

    private Account createAccount(String name, String email, Role role) {
        Account account = new Account();
        account.setName(name);
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(PASSWORD));
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

    private Deliverable createDeliverable(WorkOrder order, String urlFile) {
        Deliverable deliverable = new Deliverable();
        deliverable.setName("Modelo OBJ");
        deliverable.setUrlFile(urlFile);
        deliverable.setFileType("model/obj");
        deliverable.setCreatedAt(LocalDateTime.now());
        deliverable.setWorkOrder(order);
        return deliverableRepository.save(deliverable);
    }

    private void createPreview(Deliverable deliverable, String urlFile) {
        Preview preview = new Preview();
        preview.setCaption("Vista frontal");
        preview.setUrlFile(urlFile);
        preview.setDeliverable(deliverable);
        preview.setCreatedAt(LocalDateTime.now());
        previewRepository.save(preview);
    }
}
