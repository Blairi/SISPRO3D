package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ConflictException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.review.dto.ReviewRequest;
import com.sispro3d.unam.review.dto.ReviewResponse;
import com.sispro3d.unam.review.repository.ReviewRepository;
import com.sispro3d.unam.review.service.ReviewService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
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
class ReviewServiceImplTest {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReviewRepository reviewRepository;
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

    /** Una reseña solo existe sobre una orden COMPLETED, así que las pruebas la construyen. */
    private void createCompletedOrder(Account client, OfferedService service) {
        QuoteResponse quote = quoteService.create(QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Solicito cotización para reseñar")
                .totalAmount(new BigDecimal("1000.00"))
                .build());

        Long orderId = quoteService.accept(quote.getId(), client.getIdUser()).workOrder().getId();
        workOrderService.start(orderId, service.getExpert().getIdUser());
        workOrderService.markInReview(orderId, service.getExpert().getIdUser());
        workOrderService.complete(orderId, client.getIdUser());
    }

    /** Crea la orden en el estado indicado para probar que no habilita la reseña. */
    private Long createOrderInStatus(Account client, OfferedService service, WorkOrderStatus status) {
        QuoteResponse quote = quoteService.create(QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Orden en " + status)
                .totalAmount(new BigDecimal("1000.00"))
                .build());

        Long orderId = quoteService.accept(quote.getId(), client.getIdUser()).workOrder().getId();
        if (status == WorkOrderStatus.IN_PROGRESS || status == WorkOrderStatus.IN_REVIEW
                || status == WorkOrderStatus.COMPLETED) {
            workOrderService.start(orderId, service.getExpert().getIdUser());
        }
        if (status == WorkOrderStatus.IN_REVIEW || status == WorkOrderStatus.COMPLETED) {
            workOrderService.markInReview(orderId, service.getExpert().getIdUser());
        }
        if (status == WorkOrderStatus.COMPLETED) {
            workOrderService.complete(orderId, client.getIdUser());
        }
        return orderId;
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

    private OfferedService createService(Account expert, String title) {
        var category = new Category();
        category.setName(title + " Cat");
        category = categoryRepository.save(category);

        var service = new OfferedService();
        service.setTitle(title);
        service.setDescription("Test");
        service.setBasePrice(new BigDecimal("1000.00"));
        service.setExpert(expert);
        service.setCategory(category);
        service.setStatus(ServiceStatus.APPROVED);
        service.setDeliveryTimeDays(7);
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        return offeredServiceRepository.save(service);
    }

    @Test
    void create() {
        var client = createClient("client.create@sispro3d.com");
        var expert = createExpert("expert.create@sispro3d.com");
        var service = createService(expert, "Modelado");
        createCompletedOrder(client, service);

        var request = ReviewRequest.builder()
                .rating(5)
                .comment("Excelente trabajo, muy profesional")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();

        ReviewResponse res = reviewService.create(request);

        assertThat(res.getId()).isNotNull();
        assertThat(res.getRating()).isEqualTo(5);
        assertThat(res.getClientId()).isEqualTo(client.getIdUser());
        assertThat(res.getOfferedServiceId()).isEqualTo(service.getId());
    }

    @Test
    void findById_whenExists() {
        var client = createClient("client.find@sispro3d.com");
        var expert = createExpert("expert.find@sispro3d.com");
        var service = createService(expert, "Texturizado");
        createCompletedOrder(client, service);

        var request = ReviewRequest.builder()
                .rating(4)
                .comment("Muy buen resultado")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();
        ReviewResponse created = reviewService.create(request);

        Optional<ReviewResponse> res = reviewService.findById(created.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getRating()).isEqualTo(4);
    }

    @Test
    void create_whenNoOrderAtAll_throwsBadRequestAndStoresNothing() {
        var client = createClient("client.noorder@sispro3d.com");
        var expert = createExpert("expert.noorder@sispro3d.com");
        var service = createService(expert, "Sin orden");

        var request = ReviewRequest.builder()
                .rating(5)
                .comment("Sin orden completada")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();

        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("orden de trabajo completada");

        assertThat(reviewRepository.findByClient_IdUserAndOfferedService_Id(
                client.getIdUser(), service.getId())).isEmpty();
    }

    @Test
    void create_whenOrderStillOpen_throwsBadRequestAndStoresNothing() {
        var client = createClient("client.openorder@sispro3d.com");
        var expert = createExpert("expert.openorder@sispro3d.com");
        var service = createService(expert, "Orden abierta");
        createOrderInStatus(client, service, WorkOrderStatus.IN_REVIEW);

        var request = ReviewRequest.builder()
                .rating(5)
                .comment("Todavía en revisión")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();

        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("orden de trabajo completada");

        assertThat(reviewRepository.findByClient_IdUserAndOfferedService_Id(
                client.getIdUser(), service.getId())).isEmpty();
    }

    @Test
    void create_whenOrderCanceled_throwsBadRequestAndStoresNothing() {
        var client = createClient("client.canceled@sispro3d.com");
        var expert = createExpert("expert.canceled@sispro3d.com");
        var service = createService(expert, "Orden cancelada");
        Long orderId = createOrderInStatus(client, service, WorkOrderStatus.PENDING);
        workOrderService.cancel(orderId, client.getIdUser());

        var request = ReviewRequest.builder()
                .rating(1)
                .comment("Cancelada")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();

        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(BadRequestException.class);

        assertThat(reviewRepository.findByClient_IdUserAndOfferedService_Id(
                client.getIdUser(), service.getId())).isEmpty();
    }

    @Test
    void create_whenDuplicateReview_throwsConflictAndKeepsOriginal() {
        var client = createClient("client.dup@sispro3d.com");
        var expert = createExpert("expert.dup@sispro3d.com");
        var service = createService(expert, "Reseña duplicada");
        createCompletedOrder(client, service);

        ReviewResponse first = reviewService.create(ReviewRequest.builder()
                .rating(4)
                .comment("Primera reseña")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build());

        assertThatThrownBy(() -> reviewService.create(ReviewRequest.builder()
                .rating(1)
                .comment("Segunda reseña")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya publicó una reseña");

        assertThat(reviewRepository.findByClient_IdUserAndOfferedService_Id(
                client.getIdUser(), service.getId())).isPresent();
        assertThat(reviewService.findById(first.getId()).orElseThrow().getRating()).isEqualTo(4);
    }

    @Test
    void create_whenRatingOutOfRange_throwsBadRequestAndStoresNothing() {
        var client = createClient("client.rating@sispro3d.com");
        var expert = createExpert("expert.rating@sispro3d.com");
        var service = createService(expert, "Calificación inválida");
        createCompletedOrder(client, service);

        assertThatThrownBy(() -> reviewService.create(ReviewRequest.builder()
                .rating(6)
                .comment("Fuera de rango")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("entre 1 y 5");

        assertThatThrownBy(() -> reviewService.create(ReviewRequest.builder()
                .rating(0)
                .comment("Fuera de rango")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build()))
                .isInstanceOf(BadRequestException.class);

        assertThat(reviewRepository.findByClient_IdUserAndOfferedService_Id(
                client.getIdUser(), service.getId())).isEmpty();
    }

    @Test
    void create_whenActorIsNotClient_throwsForbiddenAndStoresNothing() {
        var client = createClient("client.forbidden@sispro3d.com");
        var expert = createExpert("expert.forbidden@sispro3d.com");
        var service = createService(expert, "Actor no cliente");
        createCompletedOrder(client, service);

        assertThatThrownBy(() -> reviewService.create(ReviewRequest.builder()
                .rating(5)
                .comment("No soy cliente")
                .clientId(expert.getIdUser())
                .offeredServiceId(service.getId())
                .build()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("CLIENT");

        assertThat(reviewRepository.findByClient_IdUserAndOfferedService_Id(
                expert.getIdUser(), service.getId())).isEmpty();
    }

    @Test
    void findById_whenNotExists() {
        Optional<ReviewResponse> res = reviewService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var client = createClient("client.findAll@sispro3d.com");
        var expert = createExpert("expert.findAll@sispro3d.com");
        var service = createService(expert, "Animacion");
        createCompletedOrder(client, service);

        var request = ReviewRequest.builder()
                .rating(3)
                .comment("Aceptable")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();
        reviewService.create(request);

        List<ReviewResponse> res = reviewService.findAll();
        assertThat(res).isNotEmpty();
    }

    @Test
    void update_whenExists() {
        var client = createClient("client.update@sispro3d.com");
        var expert = createExpert("expert.update@sispro3d.com");
        var service = createService(expert, "Rigging");
        createCompletedOrder(client, service);

        var createRequest = ReviewRequest.builder()
                .rating(3)
                .comment("Regular")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();
        ReviewResponse created = reviewService.create(createRequest);

        var updateRequest = ReviewRequest.builder()
                .rating(5)
                .comment("Revisando de nuevo, quedo excelente")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();
        ReviewResponse updated = reviewService.update(created.getId(), updateRequest);

        assertThat(updated.getRating()).isEqualTo(5);
        assertThat(updated.getComment()).contains("excelente");
    }

    @Test
    void update_whenNotExists() {
        var request = ReviewRequest.builder()
                .rating(5)
                .build();

        assertThatThrownBy(() -> reviewService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var client = createClient("client.delete@sispro3d.com");
        var expert = createExpert("expert.delete@sispro3d.com");
        var service = createService(expert, "Borrar");
        createCompletedOrder(client, service);

        var request = ReviewRequest.builder()
                .rating(2)
                .comment("Para borrar")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();
        ReviewResponse created = reviewService.create(request);

        reviewService.delete(created.getId());

        assertThat(reviewRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> reviewService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var client = createClient("client.exists@sispro3d.com");
        var expert = createExpert("expert.exists@sispro3d.com");
        var service = createService(expert, "Existe");
        createCompletedOrder(client, service);

        var request = ReviewRequest.builder()
                .rating(4)
                .comment("Test exist")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();
        ReviewResponse created = reviewService.create(request);

        assertThat(reviewService.existsById(created.getId())).isTrue();
    }

    @Test
    void findByClientId() {
        var client = createClient("client.findByClient@sispro3d.com");
        var expert = createExpert("expert.findByClient@sispro3d.com");
        var service = createService(expert, "Servicio1");
        createCompletedOrder(client, service);

        var request = ReviewRequest.builder()
                .rating(5)
                .comment("Muy bueno")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();
        reviewService.create(request);

        List<ReviewResponse> res = reviewService.findByClientId(client.getIdUser());

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getClientId()).isEqualTo(client.getIdUser());
    }

    @Test
    void findByClientId_whenNoReviews() {
        var client = createClient("client.empty@sispro3d.com");

        List<ReviewResponse> res = reviewService.findByClientId(client.getIdUser());

        assertThat(res).isEmpty();
    }

    @Test
    void findByClientId_whenIdDoesNotExist() {
        List<ReviewResponse> res = reviewService.findByClientId(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findByOfferedServiceId() {
        var client = createClient("client.findByService@sispro3d.com");
        var expert = createExpert("expert.findByService@sispro3d.com");
        var service = createService(expert, "Servicio2");
        createCompletedOrder(client, service);

        var request = ReviewRequest.builder()
                .rating(4)
                .comment("Buen servicio")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();
        reviewService.create(request);

        List<ReviewResponse> res = reviewService.findByOfferedServiceId(service.getId());

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getOfferedServiceId()).isEqualTo(service.getId());
    }

    @Test
    void findByOfferedServiceId_whenServiceDoesNotExist() {
        List<ReviewResponse> res = reviewService.findByOfferedServiceId(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findByClientIdAndOfferedServiceId_whenExists() {
        var client = createClient("client.findByBoth@sispro3d.com");
        var expert = createExpert("expert.findByBoth@sispro3d.com");
        var service = createService(expert, "Servicio3");
        createCompletedOrder(client, service);

        var request = ReviewRequest.builder()
                .rating(3)
                .comment("Regular")
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .build();
        reviewService.create(request);

        Optional<ReviewResponse> res = reviewService.findByClientIdAndOfferedServiceId(
                client.getIdUser(), service.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getRating()).isEqualTo(3);
    }

    @Test
    void findByClientIdAndOfferedServiceId_whenNotExists() {
        var client = createClient("client.nofind@sispro3d.com");
        var expert = createExpert("expert.nofind@sispro3d.com");
        var service = createService(expert, "Servicio4");

        Optional<ReviewResponse> res = reviewService.findByClientIdAndOfferedServiceId(
                client.getIdUser(), service.getId());

        assertThat(res).isEmpty();
    }
}
