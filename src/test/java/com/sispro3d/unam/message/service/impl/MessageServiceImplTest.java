package com.sispro3d.unam.message.service.impl;

import com.sispro3d.unam.category.domain.Category;
import com.sispro3d.unam.category.repository.CategoryRepository;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.message.dto.MessageRequest;
import com.sispro3d.unam.message.dto.MessageResponse;
import com.sispro3d.unam.message.repository.MessageRepository;
import com.sispro3d.unam.message.service.MessageService;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.thread.dto.ThreadRequest;
import com.sispro3d.unam.thread.dto.ThreadResponse;
import com.sispro3d.unam.thread.service.ThreadService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
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
class MessageServiceImplTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

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
    private ThreadService threadService;

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

    private ThreadResponse createThread(Account client, OfferedService service) {
        var quoteRequest = QuoteRequest.builder()
                .clientId(client.getIdUser())
                .offeredServiceId(service.getId())
                .description("Solicito cotización para mi proyecto")
                .totalAmount(new BigDecimal("1500.00"))
                .build();
        QuoteResponse quote = quoteService.create(quoteRequest);
        QuoteResponse accepted = quoteService.accept(quote.getId(), client.getIdUser());

        var orderRequest = WorkOrderRequest.builder()
                .clientId(client.getIdUser())
                .quoteId(accepted.getId())
                .build();
        WorkOrderResponse order = workOrderService.create(orderRequest);

        var threadRequest = ThreadRequest.builder()
                .actorId(client.getIdUser())
                .workOrderId(order.getId())
                .build();
        return threadService.create(threadRequest);
    }

    private MessageRequest createMessageRequest(Account user, ThreadResponse thread) {
        return MessageRequest.builder()
                .userId(user.getIdUser())
                .threadId(thread.getId())
                .content("¿Cómo va el modelo?")
                .build();
    }

    @Test
    void create_byClient() {
        var expert = createExpert("message.expert.create@sispro3d.com");
        var client = createClient("message.client.create@sispro3d.com");
        var service = createApprovedService("Modelado de personaje", expert);
        ThreadResponse thread = createThread(client, service);

        MessageResponse res = messageService.create(createMessageRequest(client, thread));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getContent()).isEqualTo("¿Cómo va el modelo?");
        assertThat(res.getThreadId()).isEqualTo(thread.getId());
        assertThat(res.getUserId()).isEqualTo(client.getIdUser());
        assertThat(res.getTimestamp()).isNotNull();
    }

    @Test
    void create_byExpert() {
        var expert = createExpert("message.expert.create2@sispro3d.com");
        var client = createClient("message.client.create2@sispro3d.com");
        var service = createApprovedService("Texturizado PBR", expert);
        ThreadResponse thread = createThread(client, service);

        MessageResponse res = messageService.create(createMessageRequest(expert, thread));

        assertThat(res.getId()).isNotNull();
        assertThat(res.getUserId()).isEqualTo(expert.getIdUser());
    }

    @Test
    void create_whenThreadDoesNotExist_throwsResourceNotFound() {
        var client = createClient("message.client.nothread@sispro3d.com");

        var request = MessageRequest.builder()
                .userId(client.getIdUser())
                .threadId(999L)
                .content("Hola")
                .build();

        assertThatThrownBy(() -> messageService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenAccountDoesNotExist_throwsResourceNotFound() {
        var expert = createExpert("message.expert.nouser@sispro3d.com");
        var client = createClient("message.client.nouser@sispro3d.com");
        var service = createApprovedService("Servicio sin usuario", expert);
        ThreadResponse thread = createThread(client, service);

        var request = MessageRequest.builder()
                .userId(999L)
                .threadId(thread.getId())
                .content("Hola")
                .build();

        assertThatThrownBy(() -> messageService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenActorNotParticipant_throwsException() {
        var expert = createExpert("message.expert.owner@sispro3d.com");
        var otherExpert = createExpert("message.expert.other@sispro3d.com");
        var client = createClient("message.client.owner@sispro3d.com");
        var service = createApprovedService("Servicio del experto", expert);
        ThreadResponse thread = createThread(client, service);

        assertThatThrownBy(() -> messageService.create(createMessageRequest(otherExpert, thread)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cliente o el experto");
    }

    @Test
    void findById_whenExists() {
        var expert = createExpert("message.expert.find@sispro3d.com");
        var client = createClient("message.client.find@sispro3d.com");
        var service = createApprovedService("Animacion de personaje", expert);
        ThreadResponse thread = createThread(client, service);
        MessageResponse created = messageService.create(createMessageRequest(client, thread));

        Optional<MessageResponse> res = messageService.findById(created.getId());

        assertThat(res).isPresent();
        assertThat(res.get().getContent()).isEqualTo("¿Cómo va el modelo?");
    }

    @Test
    void findById_whenNotExists() {
        Optional<MessageResponse> res = messageService.findById(999L);
        assertThat(res).isEmpty();
    }

    @Test
    void findAll() {
        var expert = createExpert("message.expert.findAll@sispro3d.com");
        var client = createClient("message.client.findAll@sispro3d.com");
        var service = createApprovedService("Rigging basico", expert);
        ThreadResponse thread = createThread(client, service);
        messageService.create(createMessageRequest(client, thread));

        List<MessageResponse> res = messageService.findAll();
        assertThat(res).isNotEmpty();
    }

    @Test
    void update_whenExists() {
        var expert = createExpert("message.expert.update@sispro3d.com");
        var client = createClient("message.client.update@sispro3d.com");
        var service = createApprovedService("Servicio actualizable", expert);
        ThreadResponse thread = createThread(client, service);
        MessageResponse created = messageService.create(createMessageRequest(client, thread));

        var updateRequest = MessageRequest.builder()
                .userId(client.getIdUser())
                .threadId(thread.getId())
                .content("Mensaje editado")
                .build();
        MessageResponse updated = messageService.update(created.getId(), updateRequest);

        assertThat(updated.getContent()).isEqualTo("Mensaje editado");
        assertThat(updated.getThreadId()).isEqualTo(thread.getId());
    }

    @Test
    void update_whenNotExists() {
        var request = MessageRequest.builder()
                .content("No existe")
                .build();

        assertThatThrownBy(() -> messageService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists() {
        var expert = createExpert("message.expert.delete@sispro3d.com");
        var client = createClient("message.client.delete@sispro3d.com");
        var service = createApprovedService("Para borrar", expert);
        ThreadResponse thread = createThread(client, service);
        MessageResponse created = messageService.create(createMessageRequest(client, thread));

        messageService.delete(created.getId());

        assertThat(messageRepository.existsById(created.getId())).isFalse();
    }

    @Test
    void delete_whenNotExists() {
        assertThatThrownBy(() -> messageService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsById() {
        var expert = createExpert("message.expert.exists@sispro3d.com");
        var client = createClient("message.client.exists@sispro3d.com");
        var service = createApprovedService("Servicio existente", expert);
        ThreadResponse thread = createThread(client, service);
        MessageResponse created = messageService.create(createMessageRequest(client, thread));

        assertThat(messageService.existsById(created.getId())).isTrue();
    }

    @Test
    void findByThreadId() {
        var expert = createExpert("message.expert.findByThread@sispro3d.com");
        var client = createClient("message.client.findByThread@sispro3d.com");
        var service = createApprovedService("Servicio con chat", expert);
        ThreadResponse thread = createThread(client, service);
        messageService.create(createMessageRequest(client, thread));

        var second = MessageRequest.builder()
                .userId(expert.getIdUser())
                .threadId(thread.getId())
                .content("Ya casi termino")
                .build();
        messageService.create(second);

        List<MessageResponse> res = messageService.findByThreadId(thread.getId());

        assertThat(res).hasSize(2);
        assertThat(res).extracting(MessageResponse::getContent)
                .containsExactlyInAnyOrder("¿Cómo va el modelo?", "Ya casi termino");
    }

    @Test
    void findByThreadId_whenNoMessages() {
        List<MessageResponse> res = messageService.findByThreadId(999L);
        assertThat(res).isEmpty();
    }
}
