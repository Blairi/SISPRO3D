package com.sispro3d.unam.review.repository;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.review.domain.Review;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

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
    void save() {
        var client = createClient("client1@sispro3d.com");
        var expert = createExpert("expert1@sispro3d.com");
        var service = createService(expert, "Modelado");

        var review = new Review();
        review.setRating(5);
        review.setComment("Excelente trabajo");
        review.setClient(client);
        review.setOfferedService(service);
        review.setCreatedAt(LocalDateTime.now());

        var saved = reviewRepository.save(review);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRating()).isEqualTo(5);
    }

    @Test
    void findByClient() {
        var client = createClient("client2@sispro3d.com");
        var expert = createExpert("expert2@sispro3d.com");
        var service = createService(expert, "Texturizado");

        var review = new Review();
        review.setRating(4);
        review.setComment("Muy bien");
        review.setClient(client);
        review.setOfferedService(service);
        review.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        var result = reviewRepository.findByClient(client);
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getClient().equals(client));
    }

    @Test
    void findByOfferedService() {
        var client = createClient("client3@sispro3d.com");
        var expert = createExpert("expert3@sispro3d.com");
        var service = createService(expert, "Animacion");

        var review = new Review();
        review.setRating(3);
        review.setComment("Aceptable");
        review.setClient(client);
        review.setOfferedService(service);
        review.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        var result = reviewRepository.findByOfferedService(service);
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.getOfferedService().equals(service));
    }

    @Test
    void findByClientAndOfferedService() {
        var client = createClient("client4@sispro3d.com");
        var expert = createExpert("expert4@sispro3d.com");
        var service = createService(expert, "Rigging");

        var review = new Review();
        review.setRating(5);
        review.setComment("Perfecto");
        review.setClient(client);
        review.setOfferedService(service);
        review.setCreatedAt(LocalDateTime.now());
        reviewRepository.save(review);

        Optional<Review> result = reviewRepository.findByClientAndOfferedService(client, service);
        assertThat(result).isPresent();
        assertThat(result.get().getRating()).isEqualTo(5);
    }
}
