package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.QuoteDecisionDTO;
import com.sispro3d.unam.api.dto.QuoteReplyDTO;
import com.sispro3d.unam.api.dto.QuoteRequestDTO;
import com.sispro3d.unam.api.dto.QuoteResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.exception.InvalidRequestException;
import com.sispro3d.unam.api.mapper.ApiQuoteMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.offeredservice.domain.ServiceStatus;
import com.sispro3d.unam.offeredservice.repository.OfferedServiceRepository;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.domain.QuoteStatus;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ApiQuoteService {

    private final QuoteRepository quoteRepository;
    private final AccountRepository accountRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ApiQuoteMapper mapper;

    public ApiQuoteService(QuoteRepository quoteRepository,
                           AccountRepository accountRepository,
                           OfferedServiceRepository offeredServiceRepository,
                           WorkOrderRepository workOrderRepository,
                           ApiQuoteMapper mapper) {
        this.quoteRepository = quoteRepository;
        this.accountRepository = accountRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.workOrderRepository = workOrderRepository;
        this.mapper = mapper;
    }

    public QuoteResponseDTO create(QuoteRequestDTO request) {
        var client = accountRepository.findById(request.getClientId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", request.getClientId()));
        if (client.getRole() != Role.CLIENT) {
            throw new InvalidRequestException("Only CLIENT accounts can request quotes");
        }

        var service = offeredServiceRepository.findById(request.getOfferedServiceId())
                .orElseThrow(() -> ResourceNotFoundException.forId("OfferedService", request.getOfferedServiceId()));
        if (service.getStatus() != ServiceStatus.APPROVED) {
            throw new InvalidRequestException("Only APPROVED services can be quoted");
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
        return quoteRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));
    }

    public QuoteResponseDTO reply(Long id, QuoteReplyDTO request) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));

        var expert = accountRepository.findById(request.getExpertId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", request.getExpertId()));
        if (expert.getRole() != Role.EXPERT) {
            throw new InvalidRequestException("Only EXPERT accounts can reply to quotes");
        }

        var service = quote.getOfferedService();
        if (service.getExpert() == null || !expert.getIdUser().equals(service.getExpert().getIdUser())) {
            throw new InvalidRequestException("Only the expert owning the service can reply to the quote");
        }

        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new InvalidRequestException("Only PENDING quotes can be replied");
        }

        quote.setTotalAmount(request.getTotalAmount());
        quote.setValidUntil(request.getValidUntil());

        return mapper.toResponse(quoteRepository.save(quote));
    }

    public QuoteResponseDTO accept(Long id, QuoteDecisionDTO request) {
        return transition(id, request.getClientId(), QuoteStatus.ACCEPTED);
    }

    public QuoteResponseDTO reject(Long id, QuoteDecisionDTO request) {
        return transition(id, request.getClientId(), QuoteStatus.REJECTED);
    }

    public void delete(Long id) {
        if (!quoteRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Quote", id);
        }
        if (workOrderRepository.findByQuote_Id(id).isPresent()) {
            throw new DataIntegrityException("Quote with id " + id
                    + " is referenced by a work order and cannot be deleted");
        }
        quoteRepository.deleteById(id);
    }

    private QuoteResponseDTO transition(Long id, Long clientId, QuoteStatus target) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Quote", id));

        var client = accountRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (client)", clientId));
        if (client.getRole() != Role.CLIENT) {
            throw new InvalidRequestException("Only CLIENT accounts can manage quotes");
        }

        if (quote.getClient() == null || !clientId.equals(quote.getClient().getIdUser())) {
            throw new InvalidRequestException("Only the requesting client can manage this quote");
        }

        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new InvalidRequestException("Only PENDING quotes can be accepted or rejected");
        }

        quote.setStatus(target);
        return mapper.toResponse(quoteRepository.save(quote));
    }
}