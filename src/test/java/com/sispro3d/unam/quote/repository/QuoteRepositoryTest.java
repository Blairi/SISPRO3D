package com.sispro3d.unam.quote.repository;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuoteRepositoryTest {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OfferedServiceRepository offeredServiceRepository;

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

    private OfferedService createApprovedService(String title, Account expert, Category category) {
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

    private Quote createQuote(Account client, OfferedService service) {
        var quote = new Quote();
        quote.setStatus(QuoteStatus.PENDING);
        quote.setTotalAmount(new BigDecimal("1500.00"));
        quote.setDescription("Test quote");
        quote.setClient(client);
        quote.setOfferedService(service);
        quote.setCreatedAt(LocalDateTime.now());
        return quoteRepository.save(quote);
    }

    @Test
    void save() {
        var expert = createExpert("quote.expert@sispro3d.com");
        var client = createClient("quote.client@sispro3d.com");
        var category = createCategory("Modelado 3D");
        var service = createApprovedService("Modelado de personaje", expert, category);
        var quote = createQuote(client, service);

        assertThat(quote.getId()).isNotNull();
        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.PENDING);
        assertThat(quote.getClient().getIdUser()).isEqualTo(client.getIdUser());
    }

    @Test
    void findByClient() {
        var expert = createExpert("quote.expert2@sispro3d.com");
        var client = createClient("quote.client2@sispro3d.com");
        var category = createCategory("Texturizado");
        var service = createApprovedService("Texturizado PBR", expert, category);
        createQuote(client, service);

        List<Quote> result = quoteRepository.findByClient(client);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(q -> q.getClient().getIdUser().equals(client.getIdUser()));
    }

    @Test
    void findByOfferedService() {
        var expert = createExpert("quote.expert3@sispro3d.com");
        var client = createClient("quote.client3@sispro3d.com");
        var category = createCategory("Animacion");
        var service = createApprovedService("Animacion de personaje", expert, category);
        createQuote(client, service);

        List<Quote> result = quoteRepository.findByOfferedService(service);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(q -> q.getOfferedService().getId().equals(service.getId()));
    }

    @Test
    void findByOfferedService_Expert_IdUser() {
        var expert = createExpert("quote.expert4@sispro3d.com");
        var client = createClient("quote.client4@sispro3d.com");
        var category = createCategory("Rigging");
        var service = createApprovedService("Rigging completo", expert, category);
        createQuote(client, service);

        List<Quote> result = quoteRepository.findByOfferedService_Expert_IdUser(expert.getIdUser());

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(q -> q.getOfferedService().getExpert().getIdUser().equals(expert.getIdUser()));
    }

    @Test
    void findByStatus() {
        var expert = createExpert("quote.expert5@sispro3d.com");
        var client = createClient("quote.client5@sispro3d.com");
        var category = createCategory("Renderizado");
        var service = createApprovedService("Render final", expert, category);
        createQuote(client, service);

        List<Quote> result = quoteRepository.findByStatus(QuoteStatus.PENDING);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(q -> q.getStatus() == QuoteStatus.PENDING);
    }
}
