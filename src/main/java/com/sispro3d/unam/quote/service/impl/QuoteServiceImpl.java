package com.sispro3d.unam.quote.service.impl;

import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.dto.QuoteAcceptance;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.mapper.QuoteMapper;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;
    private final AccountRepository accountRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final WorkOrderService workOrderService;

    public QuoteServiceImpl(QuoteRepository quoteRepository,
                            QuoteMapper quoteMapper,
                            AccountRepository accountRepository,
                            OfferedServiceRepository offeredServiceRepository,
                            WorkOrderService workOrderService) {
        this.quoteRepository = quoteRepository;
        this.quoteMapper = quoteMapper;
        this.accountRepository = accountRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.workOrderService = workOrderService;
    }

    @Override
    public List<QuoteResponse> findAll() {
        return quoteRepository.findAll().stream()
                .map(quoteMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<QuoteResponse> findById(Long id) {
        return quoteRepository.findById(id)
                .map(quoteMapper::toResponse);
    }

    @Override
    public QuoteResponse create(QuoteRequest request) {
        var client = accountRepository.findById(request.getClientId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", request.getClientId()));

        if (client.getRole() != Role.CLIENT) {
            log.warn("Intento de cotizar por cuenta no cliente: id={}", request.getClientId());
            throw new ForbiddenException("Solo cuentas de tipo CLIENT pueden solicitar cotizaciones");
        }

        var service = offeredServiceRepository.findById(request.getOfferedServiceId())
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", request.getOfferedServiceId()));

        if (service.getStatus() != ServiceStatus.APPROVED) {
            log.warn("Intento de cotizar servicio no aprobado: id={}, estado={}", request.getOfferedServiceId(), service.getStatus());
            throw new BadRequestException("Solo se pueden cotizar servicios en estado APPROVED");
        }

        Quote quote = quoteMapper.toEntity(request);
        quote.setClient(client);
        quote.setOfferedService(service);
        quote.setStatus(QuoteStatus.PENDING);
        quote.setCreatedAt(LocalDateTime.now());

        Quote saved = quoteRepository.save(quote);
        log.info("Cotización creada: id={}, cliente={}, servicio={}", saved.getId(), request.getClientId(), request.getOfferedServiceId());
        return quoteMapper.toResponse(saved);
    }

    @Override
    public QuoteResponse update(Long id, QuoteRequest request) {
        Quote existing = quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));

        if (!accountRepository.existsById(request.getClientId())) {
            throw ResourceNotFoundException.forId("Account (client)", request.getClientId());
        }
        if (!offeredServiceRepository.existsById(request.getOfferedServiceId())) {
            throw ResourceNotFoundException.forId("OfferedService", request.getOfferedServiceId());
        }

        if (existing.getClient() == null || !request.getClientId().equals(existing.getClient().getIdUser())) {
            throw new BadRequestException("Una cotización no puede reasignarse a otro cliente");
        }
        if (!request.getOfferedServiceId().equals(existing.getOfferedService().getId())) {
            throw new BadRequestException("Una cotización no puede reasignarse a otro servicio");
        }

        quoteMapper.updateEntityFromRequest(request, existing);
        Quote updated = quoteRepository.save(existing);
        log.info("Cotización actualizada: id={}", updated.getId());
        return quoteMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!quoteRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Quote", id);
        }
        quoteRepository.deleteById(id);
        log.info("Cotización eliminada: id={}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return quoteRepository.existsById(id);
    }

    @Override
    public QuoteResponse respond(Long id, Long expertId, QuoteRequest request) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));

        var expert = accountRepository.findById(expertId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", expertId));

        if (expert.getRole() != Role.EXPERT) {
            log.warn("Intento de responder cotización por cuenta no experta: id={}", expertId);
            throw new ForbiddenException("Solo cuentas de tipo EXPERT pueden responder cotizaciones");
        }

        var service = quote.getOfferedService();
        if (service.getExpert() == null || !expert.getIdUser().equals(service.getExpert().getIdUser())) {
            log.warn("Intento de responder cotización por experto no propietario: cotización={}, experto={}", id, expertId);
            throw new ForbiddenException("Solo el experto propietario del servicio puede responder la cotización");
        }

        if (quote.getStatus() != QuoteStatus.PENDING) {
            log.warn("Intento de responder cotización no PENDING: id={}, estado={}", id, quote.getStatus());
            throw new BadRequestException("Solo se pueden responder cotizaciones en estado PENDING");
        }

        quote.setTotalAmount(request.getTotalAmount());
        quote.setValidUntil(request.getValidUntil());

        Quote updated = quoteRepository.save(quote);
        log.info("Cotización respondida: id={}, experto={}, total={}", updated.getId(), expertId, updated.getTotalAmount());
        return quoteMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public QuoteAcceptance accept(Long id, Long clientId) {
        QuoteResponse accepted = transition(id, clientId, QuoteStatus.ACCEPTED);

        WorkOrderResponse order = workOrderService.create(WorkOrderRequest.builder()
                .clientId(clientId)
                .quoteId(id)
                .build());

        return new QuoteAcceptance(accepted, order);
    }

    @Override
    public QuoteResponse reject(Long id, Long clientId) {
        return transition(id, clientId, QuoteStatus.REJECTED);
    }

    @Override
    public QuoteResponse cancel(Long id, Long clientId) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));

        if (quote.getTotalAmount() != null) {
            throw new BadRequestException("La cotización ya fue respondida; puedes aceptarla o rechazarla");
        }

        return transition(id, clientId, QuoteStatus.REJECTED);
    }

    private QuoteResponse transition(Long id, Long clientId, QuoteStatus target) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));

        var client = accountRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", clientId));

        if (client.getRole() != Role.CLIENT) {
            log.warn("Intento de gestionar cotización por cuenta no cliente: id={}", clientId);
            throw new ForbiddenException("Solo cuentas de tipo CLIENT pueden gestionar cotizaciones");
        }

        if (quote.getClient() == null || !clientId.equals(quote.getClient().getIdUser())) {
            log.warn("Intento de gestionar cotización por cliente no propietario: cotización={}, cliente={}", id, clientId);
            throw new ForbiddenException("Solo el cliente propietario puede gestionar la cotización");
        }

        if (quote.getStatus() != QuoteStatus.PENDING) {
            log.warn("Intento de gestionar cotización no PENDING: id={}, estado={}", id, quote.getStatus());
            throw new BadRequestException("Solo se pueden gestionar cotizaciones en estado PENDING");
        }

        requireVigente(quote);

        quote.setStatus(target);
        Quote updated = quoteRepository.save(quote);
        log.info("Cotización {}: id={}, cliente={}", target, updated.getId(), clientId);
        return quoteMapper.toResponse(updated);
    }

    private void requireVigente(Quote quote) {
        if (quote.getTotalAmount() == null) {
            return;
        }
        if (quote.getValidUntil() != null && quote.getValidUntil().isBefore(LocalDate.now())) {
            throw new BadRequestException("La vigencia de la cotización expiró el " + quote.getValidUntil());
        }
    }

    @Override
    public QuoteResponse expire(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));

        if (quote.getStatus() != QuoteStatus.PENDING) {
            log.warn("Intento de expirar cotización no PENDING: id={}, estado={}", id, quote.getStatus());
            throw new BadRequestException("Solo se pueden expirar cotizaciones en estado PENDING");
        }

        quote.setStatus(QuoteStatus.EXPIRED);
        Quote updated = quoteRepository.save(quote);
        log.info("Cotización EXPIRED: id={}", updated.getId());
        return quoteMapper.toResponse(updated);
    }

    @Override
    public List<QuoteResponse> findByClientId(Long clientId) {
        return quoteRepository.findByClient_IdUser(clientId).stream()
                .map(quoteMapper::toResponse)
                .toList();
    }

    @Override
    public List<QuoteResponse> findByServiceId(Long offeredServiceId) {
        return quoteRepository.findByOfferedService_Id(offeredServiceId).stream()
                .map(quoteMapper::toResponse)
                .toList();
    }

    @Override
    public List<QuoteResponse> findByExpertId(Long expertId) {
        return quoteRepository.findByOfferedService_Expert_IdUser(expertId).stream()
                .map(quoteMapper::toResponse)
                .toList();
    }

    @Override
    public List<QuoteResponse> findByStatus(QuoteStatus status) {
        return quoteRepository.findByStatus(status).stream()
                .map(quoteMapper::toResponse)
                .toList();
    }
}
