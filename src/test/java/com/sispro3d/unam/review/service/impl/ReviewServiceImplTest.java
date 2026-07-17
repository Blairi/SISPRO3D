package com.sispro3d.unam.review.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
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
    void findById_whenNotExists() {
        Optional<ReviewResponse> res = reviewService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var client = createClient("client.findAll@sispro3d.com");
        var expert = createExpert("expert.findAll@sispro3d.com");
        var service = createService(expert, "Animacion");

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
