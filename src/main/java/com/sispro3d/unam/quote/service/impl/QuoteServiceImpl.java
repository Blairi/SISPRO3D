package com.sispro3d.unam.quote.service.impl;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.mapper.QuoteMapper;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;
    private final AccountRepository accountRepository;
    private final OfferedServiceRepository offeredServiceRepository;

    public QuoteServiceImpl(QuoteRepository quoteRepository,
                            QuoteMapper quoteMapper,
                            AccountRepository accountRepository,
                            OfferedServiceRepository offeredServiceRepository) {
        this.quoteRepository = quoteRepository;
        this.quoteMapper = quoteMapper;
        this.accountRepository = accountRepository;
        this.offeredServiceRepository = offeredServiceRepository;
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
            throw new IllegalStateException("Solo cuentas de tipo CLIENT pueden solicitar cotizaciones");
        }

        var service = offeredServiceRepository.findById(request.getOfferedServiceId())
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", request.getOfferedServiceId()));

        if (service.getStatus() != ServiceStatus.APPROVED) {
            throw new IllegalStateException("Solo se pueden cotizar servicios en estado APPROVED");
        }

        Quote quote = quoteMapper.toEntity(request);
        quote.setClient(client);
        quote.setOfferedService(service);
        quote.setStatus(QuoteStatus.PENDING);
        quote.setCreatedAt(LocalDateTime.now());

        Quote saved = quoteRepository.save(quote);
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

        quoteMapper.updateEntityFromRequest(request, existing);
        existing.setClient(accountRepository.getReferenceById(request.getClientId()));
        existing.setOfferedService(offeredServiceRepository.getReferenceById(request.getOfferedServiceId()));
        Quote updated = quoteRepository.save(existing);
        return quoteMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!quoteRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Quote", id);
        }
        quoteRepository.deleteById(id);
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
            throw new IllegalStateException("Solo cuentas de tipo EXPERT pueden responder cotizaciones");
        }

        var service = quote.getOfferedService();
        if (service.getExpert() == null || !expert.getIdUser().equals(service.getExpert().getIdUser())) {
            throw new IllegalStateException("Solo el experto propietario del servicio puede responder la cotización");
        }

        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden responder cotizaciones en estado PENDING");
        }

        quote.setTotalAmount(request.getTotalAmount());
        quote.setValidUntil(request.getValidUntil());

        Quote updated = quoteRepository.save(quote);
        return quoteMapper.toResponse(updated);
    }

    @Override
    public QuoteResponse accept(Long id, Long clientId) {
        return transition(id, clientId, QuoteStatus.ACCEPTED);
    }

    @Override
    public QuoteResponse reject(Long id, Long clientId) {
        return transition(id, clientId, QuoteStatus.REJECTED);
    }

    private QuoteResponse transition(Long id, Long clientId, QuoteStatus target) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));

        var client = accountRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", clientId));

        if (client.getRole() != Role.CLIENT) {
            throw new IllegalStateException("Solo cuentas de tipo CLIENT pueden gestionar cotizaciones");
        }

        if (quote.getClient() == null || !clientId.equals(quote.getClient().getIdUser())) {
            throw new IllegalStateException("Solo el cliente propietario puede gestionar la cotización");
        }

        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden gestionar cotizaciones en estado PENDING");
        }

        quote.setStatus(target);
        Quote updated = quoteRepository.save(quote);
        return quoteMapper.toResponse(updated);
    }

    @Override
    public QuoteResponse expire(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));

        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden expirar cotizaciones en estado PENDING");
        }

        quote.setStatus(QuoteStatus.EXPIRED);
        Quote updated = quoteRepository.save(quote);
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
