package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.QuoteAcceptanceDTO;
import com.sispro3d.unam.api.dto.QuoteReplyDTO;
import com.sispro3d.unam.api.dto.QuoteRequestDTO;
import com.sispro3d.unam.api.dto.QuoteResponseDTO;
import com.sispro3d.unam.api.dto.WorkOrderResponseDTO;
import com.sispro3d.unam.api.mapper.ApiQuoteMapper;
import com.sispro3d.unam.api.mapper.ApiWorkOrderMapper;
import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ConflictException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.security.ApiCurrentUser;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Capa API del ciclo de cotización. El actor es siempre la cuenta del token de
 * acceso y las transiciones delegan en {@link com.sispro3d.unam.quote.service.QuoteService},
 * que es quien aplica la máquina de estados.
 */
@Service
public class ApiQuoteService {

    private final QuoteRepository quoteRepository;
    private final AccountRepository accountRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final QuoteService quoteService;
    private final ApiQuoteMapper mapper;
    private final ApiWorkOrderMapper workOrderMapper;

    public ApiQuoteService(QuoteRepository quoteRepository,
                           AccountRepository accountRepository,
                           OfferedServiceRepository offeredServiceRepository,
                           WorkOrderRepository workOrderRepository,
                           QuoteService quoteService,
                           ApiQuoteMapper mapper,
                           ApiWorkOrderMapper workOrderMapper) {
        this.quoteRepository = quoteRepository;
        this.accountRepository = accountRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.workOrderRepository = workOrderRepository;
        this.quoteService = quoteService;
        this.mapper = mapper;
        this.workOrderMapper = workOrderMapper;
    }

    public QuoteResponseDTO create(QuoteRequestDTO request) {
        Long clientId = ApiCurrentUser.requireAccountId();
        var client = requireRole(clientId, Role.CLIENT, "Account (client)",
                "Only CLIENT accounts can request quotes");

        var service = offeredServiceRepository.findById(request.getOfferedServiceId())
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", request.getOfferedServiceId()));
        if (service.getStatus() != ServiceStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED services can be quoted");
        }

        Quote quote = new Quote();
        quote.setClient(client);
        quote.setOfferedService(service);
        quote.setStatus(QuoteStatus.PENDING);
        quote.setDescription(request.getDescription());
        quote.setCreatedAt(LocalDateTime.now());

        return mapper.toResponse(quoteRepository.save(quote));
    }

    public QuoteResponseDTO findById(Long id) {
        return mapper.toResponse(findQuote(id));
    }

    /**
     * Listado acotado a la identidad del token: un cliente ve sus propias
     * cotizaciones y un experto las de sus servicios.
     */
    public List<QuoteResponseDTO> findAll() {
        Account actor = requireActor();
        return switch (actor.getRole()) {
            case CLIENT -> quoteRepository.findByClient_IdUser(actor.getIdUser()).stream()
                    .map(mapper::toResponse)
                    .toList();
            case EXPERT -> quoteRepository.findByOfferedService_Expert_IdUser(actor.getIdUser()).stream()
                    .map(mapper::toResponse)
                    .toList();
            default -> quoteRepository.findAll().stream()
                    .map(mapper::toResponse)
                    .toList();
        };
    }

    public List<QuoteResponseDTO> findByStatus(QuoteStatus status) {
        Account actor = requireActor();
        return switch (actor.getRole()) {
            case CLIENT -> quoteRepository.findByStatus(status).stream()
                    .filter(q -> belongsToClient(q, actor.getIdUser()))
                    .map(mapper::toResponse)
                    .toList();
            case EXPERT -> quoteRepository.findByStatus(status).stream()
                    .filter(q -> belongsToExpert(q, actor.getIdUser()))
                    .map(mapper::toResponse)
                    .toList();
            default -> quoteRepository.findByStatus(status).stream()
                    .map(mapper::toResponse)
                    .toList();
        };
    }

    public List<QuoteResponseDTO> findByService(Long offeredServiceId) {
        if (!offeredServiceRepository.existsById(offeredServiceId)) {
            throw ResourceNotFoundException.forId("OfferedService", offeredServiceId);
        }
        return quoteRepository.findByOfferedService_Id(offeredServiceId).stream()
                .filter(this::isParticipantOfQuoteService)
                .map(mapper::toResponse)
                .toList();
    }

    public QuoteResponseDTO reply(Long id, QuoteReplyDTO request) {
        Long expertId = ApiCurrentUser.requireAccountId();

        quoteService.respond(id, expertId, QuoteRequest.builder()
                .totalAmount(request.getTotalAmount())
                .validUntil(request.getValidUntil())
                .build());

        return mapper.toResponse(findQuote(id));
    }

    /**
     * Aceptar delega en el dominio, que valida la vigencia y crea la orden de
     * trabajo en la misma transacción. La respuesta lleva ambas cosas.
     */
    public QuoteAcceptanceDTO accept(Long id) {
        Long clientId = ApiCurrentUser.requireAccountId();
        var acceptance = quoteService.accept(id, clientId);

        return QuoteAcceptanceDTO.builder()
                .quote(mapper.toResponse(findQuote(acceptance.quote().getId())))
                .workOrder(workOrderRepository.findByQuote_Id(id)
                        .map(workOrderMapper::toResponse)
                        .orElseThrow(() -> new IllegalStateException("The accepted quote did not produce a work order")))
                .build();
    }

    public QuoteResponseDTO reject(Long id) {
        Long clientId = ApiCurrentUser.requireAccountId();
        quoteService.reject(id, clientId);
        return mapper.toResponse(findQuote(id));
    }

    /**
     * Expira una cotización cuya vigencia ya venció. Delegar sin comprobar la
     * fecha dejaría pasar cotizaciones vigentes.
     */
    public QuoteResponseDTO expire(Long id) {
        var quote = findQuote(id);
        var actor = requireActor();

        boolean isClient = belongsToClient(quote, actor.getIdUser());
        boolean isExpert = belongsToExpert(quote, actor.getIdUser());
        if (!isClient && !isExpert) {
            throw new ForbiddenException("Only the requesting client or the expert of the service can expire this quote");
        }

        if (quote.getValidUntil() == null || !quote.getValidUntil().isBefore(LocalDate.now())) {
            throw new BadRequestException("The quote is still valid until " + quote.getValidUntil());
        }

        quoteService.expire(id);
        return mapper.toResponse(findQuote(id));
    }

    public void delete(Long id) {
        if (!quoteRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Quote", id);
        }
        if (workOrderRepository.findByQuote_Id(id).isPresent()) {
            throw new ConflictException("Quote with id " + id
                    + " is referenced by a work order and cannot be deleted");
        }
        quoteRepository.deleteById(id);
    }

    private Quote findQuote(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));
    }

    private Account requireActor() {
        Long actorId = ApiCurrentUser.requireAccountId();
        return accountRepository.findById(actorId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", actorId));
    }

    private Account requireRole(Long accountId, Role role, String entity, String message) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> ResourceNotFoundException.forId(entity, accountId));
        if (account.getRole() != role) {
            throw new ForbiddenException(message);
        }
        return account;
    }

    private boolean belongsToClient(Quote quote, Long accountId) {
        return quote.getClient() != null && accountId.equals(quote.getClient().getIdUser());
    }

    private boolean belongsToExpert(Quote quote, Long accountId) {
        var expert = quote.getOfferedService() == null ? null : quote.getOfferedService().getExpert();
        return expert != null && accountId.equals(expert.getIdUser());
    }

    private boolean isParticipantOfQuoteService(Quote quote) {
        Long actorId = ApiCurrentUser.requireAccountId();
        return belongsToClient(quote, actorId) || belongsToExpert(quote, actorId);
    }
}
