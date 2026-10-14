package com.sispro3d.unam.api;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.storage.FileStorageService;
import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.preview.dto.PreviewRequest;
import com.sispro3d.unam.preview.dto.PreviewResponse;
import com.sispro3d.unam.preview.service.PreviewService;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.security.JwtTokenService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiUploadCleanupTest {

    @TempDir
    static Path uploadsDir;

    @DynamicPropertySource
    static void storageLocation(DynamicPropertyRegistry registry) {
        registry.add("app.storage.location", () -> uploadsDir.toString());
    }

    @Autowired
    private MockMvc mockMvc;
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
    private DeliverableService deliverableService;
    @Autowired
    private PreviewService previewService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private JwtTokenService jwtTokenService;

    private Account expert;
    private Long orderId;

    @BeforeEach
    void setUp() {
        expert = createAccount("api.upload.expert@sispro3d.com", Role.EXPERT);
        Account client = createAccount("api.upload.client@sispro3d.com", Role.CLIENT);

        Category category = new Category();
        category.setName("Modelado API");
        category.setDescription("Servicios 3D");
        category = categoryRepository.save(category);

        OfferedService service = new OfferedService();
        service.setTitle("Modelado API");
        service.setDescription("Test description");
        service.setBasePrice(new BigDecimal("1500.00"));
        service.setExpert(expert);
        service.setCategory(category);
        service.setStatus(ServiceStatus.APPROVED);
        service.setDeliveryTimeDays(7);
        service.setCreatedAt(LocalDateTime.now());
        service = offeredServiceRepository.save(service);

        var quote = quoteService.create(QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Solicito cotización para mi proyecto")
                .totalAmount(new BigDecimal("1500.00"))
                .build());
        var accepted = quoteService.accept(quote.getId(), client.getIdUser());
        orderId = workOrderService.start(accepted.workOrder().getId(), expert.getIdUser()).getId();
    }

    private Account createAccount(String email, Role role) {
        Account account = new Account();
        account.setName("Api");
        account.setLastName("Upload");
        account.setEmail(email);
        account.setPassword("test123");
        account.setRole(role);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private String bearer() {
        return "Bearer " + jwtTokenService.issueAccessToken(expert.getIdUser(), Role.EXPERT);
    }

    private DeliverableResponse seedDeliverable() {
        return deliverableService.create(DeliverableRequest.builder()
                .expertId(expert.getIdUser())
                .workOrderId(orderId)
                .name("Modelo base")
                .file(new MockMultipartFile("file", "modelo.obj", "application/octet-stream", "x".getBytes()))
                .build());
    }

    @Test
    void apiDeleteDeliverable_removesStoredFile() throws Exception {
        DeliverableResponse deliverable = seedDeliverable();
        Path stored = fileStorageService.resolve(deliverable.getUrlFile());
        assertThat(Files.exists(stored)).isTrue();

        mockMvc.perform(delete("/api/v1/deliverables/{id}", deliverable.getId())
                        .header("Authorization", bearer()))
                .andExpect(status().isNoContent());

        assertThat(Files.exists(stored)).isFalse();
    }

    @Test
    void apiDeletePreview_removesStoredFile() throws Exception {
        DeliverableResponse deliverable = seedDeliverable();
        PreviewResponse preview = previewService.create(PreviewRequest.builder()
                .expertId(expert.getIdUser())
                .deliverableId(deliverable.getId())
                .caption("Vista frontal")
                .file(new MockMultipartFile("file", "render.png", "image/png", "x".getBytes()))
                .build());
        Path stored = fileStorageService.resolve(preview.getUrlFile());
        assertThat(Files.exists(stored)).isTrue();

        mockMvc.perform(delete("/api/v1/previews/{id}", preview.getId())
                        .header("Authorization", bearer()))
                .andExpect(status().isNoContent());

        assertThat(Files.exists(stored)).isFalse();
    }
}
