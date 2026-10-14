package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.storage.UploadValidator;
import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.service.QuoteService;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ExpertUploadControllerTest {

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
    private DeliverableRepository deliverableRepository;
    @Autowired
    private PreviewRepository previewRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long orderId;
    private Long expertId;

    @BeforeEach
    void setUp() {
        Account expert = createAccount("upload.expert@sispro3d.com", Role.EXPERT);
        Account client = createAccount("upload.client@sispro3d.com", Role.CLIENT);
        expertId = expert.getIdUser();

        Category category = new Category();
        category.setName("Modelado 3D");
        category.setDescription("Servicios 3D");
        category = categoryRepository.save(category);

        OfferedService service = new OfferedService();
        service.setTitle("Modelado de personaje");
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
        account.setName("Upload");
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode("test123"));
        account.setRole(role);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    private MockHttpSession loginExpert() throws Exception {
        return login("upload.expert@sispro3d.com", "test123");
    }

    private MockHttpSession login(String email, String password) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().isFound())
                .andReturn().getRequest().getSession(false);
    }

    private DeliverableResponse seedDeliverable() {
        return deliverableService.create(DeliverableRequest.builder()
                .expertId(expertId)
                .workOrderId(orderId)
                .name("Modelo base")
                .file(new MockMultipartFile("file", "modelo.obj", "application/octet-stream", "x".getBytes()))
                .build());
    }

    @Test
    void registerDeliverable_withValidFile_storesIt() throws Exception {
        MockHttpSession session = loginExpert();

        mockMvc.perform(multipart("/expert/orders/{id}/deliverables", orderId)
                        .file(new MockMultipartFile("file", "modelo.obj", "application/octet-stream", "obj-bytes".getBytes()))
                        .param("name", "Modelo principal")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + orderId));

        assertThat(deliverableRepository.findByWorkOrder_Id(orderId)).hasSize(1);
    }

    @Test
    void registerDeliverable_withDisallowedType_isRejectedWithMessage() throws Exception {
        MockHttpSession session = loginExpert();

        mockMvc.perform(multipart("/expert/orders/{id}/deliverables", orderId)
                        .file(new MockMultipartFile("file", "modelo.exe", "application/octet-stream", "x".getBytes()))
                        .param("name", "Modelo invalido")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertThat(deliverableRepository.findByWorkOrder_Id(orderId)).isEmpty();
    }

    @Test
    void registerDeliverable_withOversizedFile_isRejectedWithMessage() throws Exception {
        MockHttpSession session = loginExpert();
        byte[] oversized = new byte[(int) UploadValidator.MAX_BYTES + 1];

        mockMvc.perform(multipart("/expert/orders/{id}/deliverables", orderId)
                        .file(new MockMultipartFile("file", "modelo.obj", "application/octet-stream", oversized))
                        .param("name", "Modelo enorme")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        assertThat(deliverableRepository.findByWorkOrder_Id(orderId)).isEmpty();
    }

    @Test
    void registerDeliverable_withArchiveLargerThanModelLimit_storesIt() throws Exception {
        MockHttpSession session = loginExpert();
        byte[] bundle = new byte[30 * 1024 * 1024];

        mockMvc.perform(multipart("/expert/orders/{id}/deliverables", orderId)
                        .file(new MockMultipartFile("file", "proyecto.zip", "application/zip", bundle))
                        .param("name", "Paquete del proyecto")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + orderId));

        var deliverables = deliverableRepository.findByWorkOrder_Id(orderId);
        assertThat(deliverables).hasSize(1);
        assertThat(deliverables.get(0).getUrlFile()).endsWith(".zip");
    }

    @Test
    void registerPreview_withValidImage_storesIt() throws Exception {
        DeliverableResponse deliverable = seedDeliverable();
        MockHttpSession session = loginExpert();

        mockMvc.perform(multipart("/expert/orders/{orderId}/deliverables/{deliverableId}/previews",
                        orderId, deliverable.getId())
                        .file(new MockMultipartFile("file", "render.png", "image/png", "png-bytes".getBytes()))
                        .param("caption", "Vista frontal")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/" + orderId));

        assertThat(previewRepository.findByDeliverable_Id(deliverable.getId())).hasSize(1);
    }
}
