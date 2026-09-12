package com.sispro3d.unam.api;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.message.repository.MessageRepository;
import com.sispro3d.unam.favorite.domain.FavoriteService;
import com.sispro3d.unam.favorite.repository.FavoriteServiceRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.preview.domain.Preview;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.thread.repository.ThreadRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private FavoriteServiceRepository favoriteServiceRepository;

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

    private OfferedService createApprovedService() {
        OfferedService service = createService();
        service.setStatus(ServiceStatus.APPROVED);
        return offeredServiceRepository.save(service);
    }

    private Long createSecondExpert() {
        Account expert = new Account();
        expert.setName("Expert");
        expert.setLastName("Dos");
        expert.setEmail("expert2@api.test");
        expert.setPassword("test123");
        expert.setRole(Role.EXPERT);
        expert.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(expert).getIdUser();
    }

    private Long createQuote(OfferedService service) {
        Quote quote = new Quote();
        quote.setStatus(QuoteStatus.PENDING);
        quote.setClient(accountRepository.getReferenceById(clientId));
        quote.setOfferedService(service);
        quote.setCreatedAt(LocalDateTime.now());
        return quoteRepository.save(quote).getId();
    }

    private Long createWorkOrderId() {
        OfferedService service = createApprovedService();

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
        return workOrderRepository.save(order).getId();
    }

    private Long createNonParticipant() {
        Account outsider = new Account();
        outsider.setName("Outsider");
        outsider.setLastName("Uno");
        outsider.setEmail("outsider@api.test");
        outsider.setPassword("test123");
        outsider.setRole(Role.CLIENT);
        outsider.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(outsider).getIdUser();
    }

    private Long createThreadId() {
        Long orderId = createWorkOrderId();
        Thread thread = new Thread();
        thread.setWorkOrder(workOrderRepository.getReferenceById(orderId));
        return threadRepository.save(thread).getId();
    }

    private String jsonMessage(Long authorId, String content) {
        return """
                {
                  "authorId": %d,
                  "content": "%s"
                }
                """.formatted(authorId, content);
    }

    private Long createDeliverable(WorkOrderStatus status) {
        Long orderId = createWorkOrderId();
        WorkOrder order = workOrderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        workOrderRepository.save(order);

        Deliverable deliverable = new Deliverable();
        deliverable.setName("Modelo final");
        deliverable.setUrlFile("https://files.render3d.mx/modelo.fbx");
        deliverable.setFileType("model/fbx");
        deliverable.setCreatedAt(LocalDateTime.now());
        deliverable.setWorkOrder(order);
        return deliverableRepository.save(deliverable).getId();
    }

    private String jsonPreview(Long expertId) {
        return """
                {
                  "expertId": %d,
                  "caption": "Vista frontal",
                  "urlFile": "https://files.render3d.mx/preview.png"
                }
                """.formatted(expertId);
    }

    private String jsonQuoteRequest(Long clientId, Long serviceId) {
        return """
                {
                  "clientId": %d,
                  "offeredServiceId": %d,
                  "description": "Personaje para portada"
                }
                """.formatted(clientId, serviceId);
    }

    private String jsonQuoteReply(Long expertId) {
        return """
                {
                  "expertId": %d,
                  "totalAmount": 1500.00,
                  "validUntil": "2026-10-31"
                }
                """.formatted(expertId);
    }

    private String jsonQuoteDecision(Long clientId) {
        return """
                {
                  "clientId": %d
                }
                """.formatted(clientId);
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

    @Test
    void createQuote_returns201_pending_withLocation() throws Exception {
        Long serviceId = createApprovedService().getId();
        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteRequest(clientId, serviceId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.offeredServiceId").value(serviceId))
                .andExpect(jsonPath("$.client").doesNotExist());
    }

    @Test
    void createQuote_nonClient_returns400() throws Exception {
        Long serviceId = createApprovedService().getId();
        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteRequest(expertId, serviceId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only CLIENT accounts can request quotes"));
    }

    @Test
    void createQuote_nonApprovedService_returns400() throws Exception {
        Long serviceId = createService().getId();
        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteRequest(clientId, serviceId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only APPROVED services can be quoted"));
    }

    @Test
    void createQuote_unknownClient_returns404() throws Exception {
        Long serviceId = createApprovedService().getId();
        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteRequest(99999L, serviceId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createQuote_unknownService_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteRequest(clientId, 99999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void replyQuote_owningExpert_returns200() throws Exception {
        Long quoteId = createQuote(createApprovedService());
        mockMvc.perform(put("/api/v1/quotes/{id}/reply", quoteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteReply(expertId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(1500.00))
                .andExpect(jsonPath("$.validUntil").value("2026-10-31"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void replyQuote_nonOwningExpert_returns400() throws Exception {
        Long quoteId = createQuote(createApprovedService());
        Long otherExpertId = createSecondExpert();
        mockMvc.perform(put("/api/v1/quotes/{id}/reply", quoteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteReply(otherExpertId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only the expert owning the service can reply to the quote"));
    }

    @Test
    void replyQuote_resolvedQuote_returns400() throws Exception {
        Long quoteId = createQuote(createApprovedService());
        mockMvc.perform(put("/api/v1/quotes/{id}/accept", quoteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteDecision(clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(put("/api/v1/quotes/{id}/reply", quoteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteReply(expertId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only PENDING quotes can be replied"));
    }

    @Test
    void acceptQuote_requestingClient_returnsAccepted() throws Exception {
        Long quoteId = createQuote(createApprovedService());
        mockMvc.perform(put("/api/v1/quotes/{id}/accept", quoteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteDecision(clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void acceptQuote_nonRequestingClient_returns400() throws Exception {
        Long quoteId = createQuote(createApprovedService());
        Account otherClient = new Account();
        otherClient.setName("Client");
        otherClient.setLastName("Dos");
        otherClient.setEmail("client2@api.test");
        otherClient.setPassword("test123");
        otherClient.setRole(Role.CLIENT);
        otherClient.setCreatedAt(LocalDateTime.now());
        Long otherClientId = accountRepository.save(otherClient).getIdUser();

        mockMvc.perform(put("/api/v1/quotes/{id}/accept", quoteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteDecision(otherClientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only the requesting client can manage this quote"));
    }

    @Test
    void rejectQuote_requestingClient_returnsRejected() throws Exception {
        Long quoteId = createQuote(createApprovedService());
        mockMvc.perform(put("/api/v1/quotes/{id}/reject", quoteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuoteDecision(clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void deleteQuote_returns204() throws Exception {
        Long quoteId = createQuote(createApprovedService());
        mockMvc.perform(delete("/api/v1/quotes/{id}", quoteId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteQuote_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/quotes/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteQuote_withExistingWorkOrder_returns409_withMessage() throws Exception {
        Long orderId = createWorkOrderId();
        Long quoteId = workOrderRepository.findById(orderId).orElseThrow().getQuote().getId();

        mockMvc.perform(delete("/api/v1/quotes/{id}", quoteId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Quote with id " + quoteId + " is referenced by a work order and cannot be deleted"));
    }

    @Test
    void createThread_returns201_withLocation() throws Exception {
        Long orderId = createWorkOrderId();
        String body = """
                {
                  "actorId": %d
                }
                """.formatted(clientId);
        mockMvc.perform(post("/api/v1/work-orders/{orderId}/thread", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.workOrderId").value(orderId));
    }

    @Test
    void createThread_duplicate_returns409() throws Exception {
        Long orderId = createWorkOrderId();
        String body = """
                {
                  "actorId": %d
                }
                """.formatted(clientId);
        mockMvc.perform(post("/api/v1/work-orders/{orderId}/thread", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/work-orders/{orderId}/thread", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void createThread_nonParticipant_returns400() throws Exception {
        Long orderId = createWorkOrderId();
        Long outsiderId = createNonParticipant();
        String body = """
                {
                  "actorId": %d
                }
                """.formatted(outsiderId);
        mockMvc.perform(post("/api/v1/work-orders/{orderId}/thread", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only the order's client or expert can access the thread"));
    }

    @Test
    void createThread_unknownOrder_returns404() throws Exception {
        String body = """
                {
                  "actorId": %d
                }
                """.formatted(clientId);
        mockMvc.perform(post("/api/v1/work-orders/99999/thread")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void getThreadByOrder_returns200() throws Exception {
        Long orderId = createWorkOrderId();
        String body = """
                {
                  "actorId": %d
                }
                """.formatted(clientId);
        mockMvc.perform(post("/api/v1/work-orders/{orderId}/thread", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/work-orders/{orderId}/thread", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workOrderId").value(orderId));
    }

    @Test
    void getThreadById_returns200() throws Exception {
        Long orderId = createWorkOrderId();
        String body = """
                {
                  "actorId": %d
                }
                """.formatted(clientId);
        String location = mockMvc.perform(post("/api/v1/work-orders/{orderId}/thread", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(java.net.URI.create(location).getPath()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workOrderId").value(orderId));
    }

    @Test
    void deleteThread_returns204() throws Exception {
        Long orderId = createWorkOrderId();
        String body = """
                {
                  "actorId": %d
                }
                """.formatted(clientId);
        mockMvc.perform(post("/api/v1/work-orders/{orderId}/thread", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String threadJson = mockMvc.perform(get("/api/v1/work-orders/{orderId}/thread", orderId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long threadId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(threadJson).get("id").asLong();

        mockMvc.perform(delete("/api/v1/threads/{id}", threadId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteThread_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/threads/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteThread_withMessages_returns204_andDeletesMessages() throws Exception {
        Long threadId = createThreadId();
        mockMvc.perform(post("/api/v1/threads/{threadId}/messages", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMessage(clientId, "Mensaje uno")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/threads/{threadId}/messages", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMessage(expertId, "Mensaje dos")))
                .andExpect(status().isCreated());
        assertEquals(2, messageRepository.findByThread_IdOrderByTimestampAsc(threadId).size());

        mockMvc.perform(delete("/api/v1/threads/{id}", threadId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/threads/{id}", threadId))
                .andExpect(status().isNotFound());
        assertEquals(0, messageRepository.findByThread_IdOrderByTimestampAsc(threadId).size());
    }

    @Test
    void postMessage_returns201_withAuthorAndTimestamp() throws Exception {
        Long threadId = createThreadId();
        mockMvc.perform(post("/api/v1/threads/{threadId}/messages", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMessage(clientId, "Mensaje de la prueba")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.userId").value(clientId))
                .andExpect(jsonPath("$.threadId").value(threadId))
                .andExpect(jsonPath("$.content").value("Mensaje de la prueba"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.account").doesNotExist());
    }

    @Test
    void postMessage_nonParticipant_returns400() throws Exception {
        Long threadId = createThreadId();
        Long outsiderId = createNonParticipant();
        mockMvc.perform(post("/api/v1/threads/{threadId}/messages", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMessage(outsiderId, "Intento de intruso")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only the order's client or expert can post messages"));
    }

    @Test
    void listMessages_returnsAscendingOrder() throws Exception {
        Long threadId = createThreadId();
        mockMvc.perform(post("/api/v1/threads/{threadId}/messages", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMessage(expertId, "Primer mensaje")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/threads/{threadId}/messages", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMessage(clientId, "Segundo mensaje")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/threads/{threadId}/messages", threadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].content").value("Primer mensaje"))
                .andExpect(jsonPath("$[1].content").value("Segundo mensaje"));
    }

    @Test
    void postMessage_unknownThread_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/threads/99999/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMessage(clientId, "A un hilo inexistente")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMessageById_returns200() throws Exception {
        Long threadId = createThreadId();
        String body = jsonMessage(clientId, "Mensaje individual");
        String messageJson = mockMvc.perform(post("/api/v1/threads/{threadId}/messages", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long messageId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(messageJson).get("id").asLong();

        mockMvc.perform(get("/api/v1/messages/{id}", messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Mensaje individual"));
    }

    @Test
    void deleteMessage_returns204() throws Exception {
        Long threadId = createThreadId();
        String messageJson = mockMvc.perform(post("/api/v1/threads/{threadId}/messages", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMessage(clientId, "Por borrar")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long messageId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(messageJson).get("id").asLong();

        mockMvc.perform(delete("/api/v1/messages/{id}", messageId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/messages/{id}", messageId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMessage_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/messages/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addPreview_returns201() throws Exception {
        Long deliverableId = createDeliverable(WorkOrderStatus.IN_PROGRESS);
        mockMvc.perform(post("/api/v1/deliverables/{deliverableId}/previews", deliverableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreview(expertId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deliverableId").value(deliverableId))
                .andExpect(jsonPath("$.caption").value("Vista frontal"))
                .andExpect(jsonPath("$.urlFile").value("https://files.render3d.mx/preview.png"))
                .andExpect(jsonPath("$.deliverable").doesNotExist());
    }

    @Test
    void addPreview_inactiveOrder_returns400() throws Exception {
        Long deliverableId = createDeliverable(WorkOrderStatus.COMPLETED);
        mockMvc.perform(post("/api/v1/deliverables/{deliverableId}/previews", deliverableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreview(expertId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Previews can only be added while the order is IN_PROGRESS or IN_REVIEW"));
    }

    @Test
    void addPreview_nonOwnerExpert_returns400() throws Exception {
        Long deliverableId = createDeliverable(WorkOrderStatus.IN_PROGRESS);
        Long otherExpertId = createSecondExpert();
        mockMvc.perform(post("/api/v1/deliverables/{deliverableId}/previews", deliverableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreview(otherExpertId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only the expert owning the order can add previews"));
    }

    @Test
    void listPreviews_returns200() throws Exception {
        Long deliverableId = createDeliverable(WorkOrderStatus.IN_REVIEW);
        mockMvc.perform(post("/api/v1/deliverables/{deliverableId}/previews", deliverableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreview(expertId)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/deliverables/{deliverableId}/previews", deliverableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreview(expertId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/deliverables/{deliverableId}/previews", deliverableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void addPreview_unknownDeliverable_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/deliverables/99999/previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreview(expertId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPreviewById_returns200() throws Exception {
        Long deliverableId = createDeliverable(WorkOrderStatus.IN_PROGRESS);
        String previewJson = mockMvc.perform(post("/api/v1/deliverables/{deliverableId}/previews", deliverableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreview(expertId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long previewId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(previewJson).get("id").asLong();

        mockMvc.perform(get("/api/v1/previews/{id}", previewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caption").value("Vista frontal"));
    }

    @Test
    void deletePreview_returns204() throws Exception {
        Long deliverableId = createDeliverable(WorkOrderStatus.IN_PROGRESS);
        String previewJson = mockMvc.perform(post("/api/v1/deliverables/{deliverableId}/previews", deliverableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreview(expertId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long previewId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(previewJson).get("id").asLong();

        mockMvc.perform(delete("/api/v1/previews/{id}", previewId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/previews/{id}", previewId))
                .andExpect(status().isNotFound());
    }

    @Test
    void associateFavorite_returns201_andVisibleInList() throws Exception {
        Long serviceId = createApprovedService().getId();
        mockMvc.perform(post("/api/v1/clients/{clientId}/favorite-services/{serviceId}", clientId, serviceId))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(serviceId))
                .andExpect(jsonPath("$.title").value("Modelado de personaje"));

        mockMvc.perform(get("/api/v1/clients/{clientId}/favorite-services", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(serviceId));
    }

    @Test
    void associateFavorite_duplicate_returns409() throws Exception {
        Long serviceId = createApprovedService().getId();
        mockMvc.perform(post("/api/v1/clients/{clientId}/favorite-services/{serviceId}", clientId, serviceId))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/clients/{clientId}/favorite-services/{serviceId}", clientId, serviceId))
                .andExpect(status().isConflict());
    }

    @Test
    void associateFavorite_unknownClient_returns404() throws Exception {
        Long serviceId = createApprovedService().getId();
        mockMvc.perform(post("/api/v1/clients/99999/favorite-services/{serviceId}", serviceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void associateFavorite_unknownService_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/clients/{clientId}/favorite-services/99999", clientId))
                .andExpect(status().isNotFound());
    }

    @Test
    void listFavorites_unknownClient_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/clients/99999/favorite-services"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFavorite_returns204() throws Exception {
        Long serviceId = createApprovedService().getId();
        mockMvc.perform(post("/api/v1/clients/{clientId}/favorite-services/{serviceId}", clientId, serviceId))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/clients/{clientId}/favorite-services/{serviceId}", clientId, serviceId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/clients/{clientId}/favorite-services", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void removeFavorite_missingAssociation_returns404() throws Exception {
        Long serviceId = createApprovedService().getId();
        mockMvc.perform(delete("/api/v1/clients/{clientId}/favorite-services/{serviceId}", clientId, serviceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonPositivePathParameter_returns400_withDetails() throws Exception {
        mockMvc.perform(get("/api/v1/quotes/-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").isNotEmpty());
    }
}
