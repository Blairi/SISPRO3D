package com.sispro3d.unam.quote.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuoteServiceImplTest {

    @Autowired
    private QuoteService quoteService;

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

    private OfferedService createApprovedService(String title, Account expert) {
        var category = createCategory("Cat " + title);
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

    private QuoteRequest createQuoteRequest(Account client, OfferedService service) {
        return QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Solicito cotización para mi proyecto")
                .totalAmount(new BigDecimal("1500.00"))
                .build();
    }

    private QuoteResponse createPendingQuote(Account client, OfferedService service) {
        return quoteService.create(createQuoteRequest(client, service));
    }

    @Test
    void create() {
        var expert = createExpert("quote.expert.create@sispro3d.com");
        var client = createClient("quote.client.create@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);

        QuoteResponse res = quoteService.create(createQuoteRequest(client, service));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getClientId()).isEqualTo(client.getIdUser());
        assertThat(res.getOfferedServiceId()).isEqualTo(service.getId());
        assertThat(res.getStatus()).isEqualTo(QuoteStatus.PENDING);
    }

    @Test
    void create_whenClientDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("quote.expert.noclient@sispro3d.com");
        var service = createApprovedService("Servicio sin cliente", expert);

        var request = createQuoteRequest(expert, service);
        request.setClientId(999L);

        assertThatThrownBy(() -> quoteService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenServiceDoesNotExist_throwsResourceNotFound() {
        var client = createClient("quote.client.noservice@sispro3d.com");

        var request = QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(999L)
                .description("Solicito cotización para mi proyecto")
                .totalAmount(new BigDecimal("1500.00"))
                .build();

        assertThatThrownBy(() -> quoteService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenAccountIsNotClient_throwsException() {
        var expert = createExpert("quote.expert.notclient@sispro3d.com");
        var service = createApprovedService("Servicio con experto", expert);

        var request = createQuoteRequest(expert, service);

        assertThatThrownBy(() -> quoteService.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLIENT");
    }

    @Test
    void create_whenServiceNotApproved_throwsException() {
        var expert = createExpert("quote.expert.pending@sispro3d.com");
        var client = createClient("quote.client.pending@sispro3d.com");
        var category = createCategory("Pendiente");

        var service = new OfferedService();
        service.setTitle("Servicio pendiente");
        service.setDescription("Test description");
        service.setBasePrice(new BigDecimal("1500.00"));
        service.setExpert(expert);
        service.setCategory(category);
        service.setStatus(ServiceStatus.PENDING);
        service.setDeliveryTimeDays(7);
        service.setCreatedAt(LocalDateTime.now());
        offeredServiceRepository.save(service);

        assertThatThrownBy(() -> quoteService.create(createQuoteRequest(client, service)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    void findById_whenExists() {
        var expert = createExpert("quote.expert.find@sispro3d.com");
        var client = createClient("quote.client.find@sispro3d.com");
        var service = createApprovedService("Texturizado PBR", expert);
        QuoteResponse created = createPendingQuote(client, service);

        Optional<QuoteResponse> res = quoteService.findById(created.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getOfferedServiceId()).isEqualTo(service.getId());
    }

    @Test
    void findById_whenNotExists() {
        Optional<QuoteResponse> res = quoteService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var expert = createExpert("quote.expert.findAll@sispro3d.com");
        var client = createClient("quote.client.findAll@sispro3d.com");
        var service = createApprovedService("Animacion de personaje", expert);
        createPendingQuote(client, service);

        List<QuoteResponse> res = quoteService.findAll();
        assertThat(res).isNotEmpty();
    }

    @Test
    void update_whenExists() {
        var expert = createExpert("quote.expert.update@sispro3d.com");
        var client = createClient("quote.client.update@sispro3d.com");
        var service = createApprovedService("Rigging basico", expert);
        QuoteResponse created = createPendingQuote(client, service);

        var updateRequest = QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Descripción actualizada")
                .totalAmount(new BigDecimal("2000.00"))
                .build();
        QuoteResponse updated = quoteService.update(created.getId(), updateRequest);

        assertThat(updated.getDescription()).isEqualTo("Descripción actualizada");
        assertThat(updated.getTotalAmount()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(updated.getStatus()).isEqualTo(QuoteStatus.PENDING);
    }

    @Test
    void update_whenNotExists() {
        var request = QuoteRequest.builder()
                .description("No existe")
                .build();

        assertThatThrownBy(() -> quoteService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var expert = createExpert("quote.expert.delete@sispro3d.com");
        var client = createClient("quote.client.delete@sispro3d.com");
        var service = createApprovedService("Para borrar", expert);
        QuoteResponse created = createPendingQuote(client, service);

        quoteService.delete(created.getId());

        assertThat(quoteRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> quoteService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var expert = createExpert("quote.expert.exists@sispro3d.com");
        var client = createClient("quote.client.exists@sispro3d.com");
        var service = createApprovedService("Servicio existente", expert);
        QuoteResponse created = createPendingQuote(client, service);

        assertThat(quoteService.existsById(created.getId())).isTrue();
    }

    @Test
    void respond() {
        var expert = createExpert("quote.expert.respond@sispro3d.com");
        var client = createClient("quote.client.respond@sispro3d.com");
        var service = createApprovedService("Modelado estilizado", expert);
        QuoteResponse created = createPendingQuote(client, service);

        var proposal = QuoteRequest.builder()
                .totalAmount(new BigDecimal("8500.00"))
                .validUntil(LocalDate.of(2025, 8, 1))
                .build();
        QuoteResponse res = quoteService.respond(created.getId(), expert.getIdUser(), proposal);

        assertThat(res.getTotalAmount()).isEqualTo(new BigDecimal("8500.00"));
        assertThat(res.getValidUntil()).isEqualTo(LocalDate.of(2025, 8, 1));
        assertThat(res.getStatus()).isEqualTo(QuoteStatus.PENDING);
    }

    @Test
    void respond_whenExpertDoesNotOwnService_throwsException() {
        var expert = createExpert("quote.expert.owner@sispro3d.com");
        var otherExpert = createExpert("quote.expert.other@sispro3d.com");
        var client = createClient("quote.client.respond@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        QuoteResponse created = createPendingQuote(client, service);

        var proposal = QuoteRequest.builder()
                .totalAmount(new BigDecimal("8500.00"))
                .build();

        assertThatThrownBy(() -> quoteService.respond(created.getId(), otherExpert.getIdUser(), proposal))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("propietario");
    }

    @Test
    void respond_whenAccountIsNotExpert_throwsException() {
        var expert = createExpert("quote.expert.respond2@sispro3d.com");
        var client = createClient("quote.client.respond2@sispro3d.com");
        var service = createApprovedService("Servicio 2", expert);
        QuoteResponse created = createPendingQuote(client, service);

        var proposal = QuoteRequest.builder()
                .totalAmount(new BigDecimal("8500.00"))
                .build();

        assertThatThrownBy(() -> quoteService.respond(created.getId(), client.getIdUser(), proposal))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EXPERT");
    }

    @Test
    void respond_whenQuoteNotPending_throwsException() {
        var expert = createExpert("quote.expert.respond3@sispro3d.com");
        var client = createClient("quote.client.respond3@sispro3d.com");
        var service = createApprovedService("Servicio 3", expert);
        QuoteResponse created = createPendingQuote(client, service);
        quoteService.accept(created.getId(), client.getIdUser());

        var proposal = QuoteRequest.builder()
                .totalAmount(new BigDecimal("8500.00"))
                .build();

        assertThatThrownBy(() -> quoteService.respond(created.getId(), expert.getIdUser(), proposal))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void respond_whenQuoteDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("quote.expert.respond4@sispro3d.com");

        var proposal = QuoteRequest.builder()
                .totalAmount(new BigDecimal("8500.00"))
                .build();

        assertThatThrownBy(() -> quoteService.respond(999L, expert.getIdUser(), proposal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void respond_whenExpertDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("quote.expert.respond5@sispro3d.com");
        var client = createClient("quote.client.respond5@sispro3d.com");
        var service = createApprovedService("Servicio 5", expert);
        QuoteResponse created = createPendingQuote(client, service);

        var proposal = QuoteRequest.builder()
                .totalAmount(new BigDecimal("8500.00"))
                .build();

        assertThatThrownBy(() -> quoteService.respond(created.getId(), 999L, proposal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void accept() {
        var expert = createExpert("quote.expert.accept@sispro3d.com");
        var client = createClient("quote.client.accept@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        QuoteResponse created = createPendingQuote(client, service);

        QuoteResponse accepted = quoteService.accept(created.getId(), client.getIdUser());

        assertThat(accepted.getStatus()).isEqualTo(QuoteStatus.ACCEPTED);
    }

    @Test
    void accept_whenClientDoesNotOwnQuote_throwsException() {
        var expert = createExpert("quote.expert.accept2@sispro3d.com");
        var client = createClient("quote.client.accept2@sispro3d.com");
        var otherClient = createClient("quote.client.other@sispro3d.com");
        var service = createApprovedService("Servicio aceptar", expert);
        QuoteResponse created = createPendingQuote(client, service);

        assertThatThrownBy(() -> quoteService.accept(created.getId(), otherClient.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("propietario");
    }

    @Test
    void accept_whenAccountIsNotClient_throwsException() {
        var expert = createExpert("quote.expert.accept3@sispro3d.com");
        var client = createClient("quote.client.accept3@sispro3d.com");
        var service = createApprovedService("Servicio aceptar 3", expert);
        QuoteResponse created = createPendingQuote(client, service);

        assertThatThrownBy(() -> quoteService.accept(created.getId(), expert.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLIENT");
    }

    @Test
    void accept_whenAlreadyAccepted_throwsException() {
        var expert = createExpert("quote.expert.accept4@sispro3d.com");
        var client = createClient("quote.client.accept4@sispro3d.com");
        var service = createApprovedService("Servicio aceptar 4", expert);
        QuoteResponse created = createPendingQuote(client, service);
        quoteService.accept(created.getId(), client.getIdUser());

        assertThatThrownBy(() -> quoteService.accept(created.getId(), client.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void accept_whenQuoteDoesNotExist_throwsResourceNotFound() {
        var client = createClient("quote.client.accept5@sispro3d.com");

        assertThatThrownBy(() -> quoteService.accept(999L, client.getIdUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void reject() {
        var expert = createExpert("quote.expert.reject@sispro3d.com");
        var client = createClient("quote.client.reject@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        QuoteResponse created = createPendingQuote(client, service);

        QuoteResponse rejected = quoteService.reject(created.getId(), client.getIdUser());

        assertThat(rejected.getStatus()).isEqualTo(QuoteStatus.REJECTED);
    }

    @Test
    void reject_whenClientDoesNotOwnQuote_throwsException() {
        var expert = createExpert("quote.expert.reject2@sispro3d.com");
        var client = createClient("quote.client.reject2@sispro3d.com");
        var otherClient = createClient("quote.client.reject3@sispro3d.com");
        var service = createApprovedService("Servicio rechazar", expert);
        QuoteResponse created = createPendingQuote(client, service);

        assertThatThrownBy(() -> quoteService.reject(created.getId(), otherClient.getIdUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("propietario");
    }

    @Test
    void expire() {
        var expert = createExpert("quote.expert.expire@sispro3d.com");
        var client = createClient("quote.client.expire@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        QuoteResponse created = createPendingQuote(client, service);

        QuoteResponse expired = quoteService.expire(created.getId());

        assertThat(expired.getStatus()).isEqualTo(QuoteStatus.EXPIRED);
    }

    @Test
    void expire_whenNotPending_throwsException() {
        var expert = createExpert("quote.expert.expire2@sispro3d.com");
        var client = createClient("quote.client.expire2@sispro3d.com");
        var service = createApprovedService("Servicio expirar", expert);
        QuoteResponse created = createPendingQuote(client, service);
        quoteService.accept(created.getId(), client.getIdUser());

        assertThatThrownBy(() -> quoteService.expire(created.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void findByClientId() {
        var expert = createExpert("quote.expert.findByClient@sispro3d.com");
        var client = createClient("quote.client.findByClient@sispro3d.com");
        var service = createApprovedService("Servicio del cliente", expert);
        createPendingQuote(client, service);

        List<QuoteResponse> res = quoteService.findByClientId(client.getIdUser());

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getClientId()).isEqualTo(client.getIdUser());
    }

    @Test
    void findByClientId_whenIdDoesNotExist() {
        List<QuoteResponse> res = quoteService.findByClientId(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findByServiceId() {
        var expert = createExpert("quote.expert.findByService@sispro3d.com");
        var client = createClient("quote.client.findByService@sispro3d.com");
        var service = createApprovedService("Servicio cotizado", expert);
        createPendingQuote(client, service);

        List<QuoteResponse> res = quoteService.findByServiceId(service.getId());

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getOfferedServiceId()).isEqualTo(service.getId());
    }

    @Test
    void findByExpertId() {
        var expert = createExpert("quote.expert.findByExpert@sispro3d.com");
        var client = createClient("quote.client.findByExpert@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        createPendingQuote(client, service);

        List<QuoteResponse> res = quoteService.findByExpertId(expert.getIdUser());

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getOfferedServiceId()).isEqualTo(service.getId());
    }

    @Test
    void findByExpertId_whenNoQuotes() {
        var expert = createExpert("quote.expert.empty@sispro3d.com");

        List<QuoteResponse> res = quoteService.findByExpertId(expert.getIdUser());

        assertThat(res).isEmpty();
    }

    @Test
    void findByStatus() {
        var expert = createExpert("quote.expert.findByStatus@sispro3d.com");
        var client = createClient("quote.client.findByStatus@sispro3d.com");
        var service = createApprovedService("Servicio pendiente", expert);
        createPendingQuote(client, service);

        List<QuoteResponse> pending = quoteService.findByStatus(QuoteStatus.PENDING);
        List<QuoteResponse> accepted = quoteService.findByStatus(QuoteStatus.ACCEPTED);

        assertThat(pending).isNotEmpty();
        assertThat(pending).allMatch(q -> q.getStatus() == QuoteStatus.PENDING);
        assertThat(accepted).isEmpty();
    }
}
