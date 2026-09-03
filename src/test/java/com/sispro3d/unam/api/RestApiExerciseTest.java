package com.sispro3d.unam.api;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class RestApiExerciseTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Long expertId;
    private Long clientId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        Account expert = new Account();
        expert.setName("Expert");
        expert.setLastName("Uno");
        expert.setEmail("expert@api.test");
        expert.setPassword("test123");
        expert.setRole(Role.EXPERT);
        expert.setCreatedAt(LocalDateTime.now());
        expertId = accountRepository.save(expert).getIdUser();

        Account client = new Account();
        client.setName("Client");
        client.setLastName("Uno");
        client.setEmail("client@api.test");
        client.setPassword("test123");
        client.setRole(Role.CLIENT);
        client.setCreatedAt(LocalDateTime.now());
        clientId = accountRepository.save(client).getIdUser();

        Category category = new Category();
        category.setName("Modelado");
        category.setDescription("Servicios de modelado 3D");
        categoryId = categoryRepository.save(category).getId();
    }

    private OfferedService createService() {
        OfferedService service = new OfferedService();
        service.setTitle("Modelado de personaje");
        service.setDescription("Descripcion");
        service.setBasePrice(new BigDecimal("1500.00"));
        service.setExpert(accountRepository.getReferenceById(expertId));
        service.setCategory(categoryRepository.getReferenceById(categoryId));
        service.setStatus(ServiceStatus.PENDING);
        service.setDeliveryTimeDays(7);
        service.setCreatedAt(LocalDateTime.now());
        return offeredServiceRepository.save(service);
    }

    private String jsonService() {
        return """
                {
                  "title": "Modelado de personaje",
                  "description": "Personaje estilizado",
                  "basePrice": 1500.00,
                  "expertId": %d,
                  "categoryId": %d,
                  "deliveryTimeDays": 7
                }
                """.formatted(expertId, categoryId);
    }

    private Long createDeliverableWithPreview() {
        OfferedService service = createService();

        Quote quote = new Quote();
        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setTotalAmount(new BigDecimal("1500.00"));
        quote.setClient(accountRepository.getReferenceById(clientId));
        quote.setOfferedService(service);
        quote.setCreatedAt(LocalDateTime.now());
        Quote savedQuote = quoteRepository.save(quote);

        WorkOrder order = new WorkOrder();
        order.setStatus(WorkOrderStatus.IN_PROGRESS);
        order.setQuote(savedQuote);
        order.setCreatedAt(LocalDateTime.now());
        WorkOrder savedOrder = workOrderRepository.save(order);

        Deliverable deliverable = new Deliverable();
        deliverable.setName("Modelo final");
        deliverable.setUrlFile("https://files.render3d.mx/modelo.fbx");
        deliverable.setFileType("model/fbx");
        deliverable.setCreatedAt(LocalDateTime.now());
        deliverable.setWorkOrder(savedOrder);
        Deliverable savedDeliverable = deliverableRepository.save(deliverable);

        Preview preview = new Preview();
        preview.setCaption("Vista frontal");
        preview.setUrlFile("https://files.render3d.mx/preview.png");
        preview.setDeliverable(savedDeliverable);
        previewRepository.save(preview);

        return savedDeliverable.getId();
    }

    @Test
    void deleteDeliverable_withPreview_returns204() throws Exception {
        Long deliverableId = createDeliverableWithPreview();
        mockMvc.perform(delete("/api/v1/deliverables/{id}", deliverableId))
                .andExpect(status().isNoContent());
    }

    @Test
    void createService_returns201_withLocation() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonService()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Modelado de personaje"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getService_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/services/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void createService_missingCategory_returns404() throws Exception {
        String body = """
                {
                  "title": "Sin categoria",
                  "basePrice": 100.0,
                  "expertId": %d,
                  "categoryId": 99999,
                  "deliveryTimeDays": 3
                }
                """.formatted(expertId);
        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReview_ratingOutOfRange_returns400() throws Exception {
        Long serviceId = createService().getId();
        String body = """
                {
                  "rating": 8,
                  "comment": "fuera de rango",
                  "clientId": %d
                }
                """.formatted(clientId);
        mockMvc.perform(post("/api/v1/services/{serviceId}/reviews", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").isNotEmpty());
    }

    @Test
    void createReview_duplicateClientService_returns409() throws Exception {
        Long serviceId = createService().getId();
        String body = """
                {
                  "rating": 5,
                  "comment": "muy bueno",
                  "clientId": %d
                }
                """.formatted(clientId);
        mockMvc.perform(post("/api/v1/services/{serviceId}/reviews", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/services/{serviceId}/reviews", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void listServiceReviews_returns200() throws Exception {
        Long serviceId = createService().getId();
        mockMvc.perform(get("/api/v1/services/{serviceId}/reviews", serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deleteCategory_withServices_returns409() throws Exception {
        createService();
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isConflict());
    }

    @Test
    void getAccount_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateService_returns200() throws Exception {
        Long serviceId = createService().getId();
        mockMvc.perform(put("/api/v1/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonService()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(serviceId))
                .andExpect(jsonPath("$.title").value("Modelado de personaje"));
    }

    @Test
    void deleteDeliverable_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/deliverables/99999"))
                .andExpect(status().isNotFound());
    }
}
