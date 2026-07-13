package com.sispro3d.unam.quote.service.impl;

import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.OfferedServiceRef;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.dto.QuoteRequest;
import com.sispro3d.unam.quote.dto.QuoteResponse;
import com.sispro3d.unam.quote.repository.QuoteRepository;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Account;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;

    public QuoteServiceImpl(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Override
    public List<QuoteResponse> findAll() {
        return quoteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<QuoteResponse> findById(Long id) {
        return quoteRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public QuoteResponse create(QuoteRequest request) {
        Quote quote = toEntity(request);
        Quote saved = quoteRepository.save(quote);
        return toResponse(saved);
    }

    @Override
    public QuoteResponse update(Long id, QuoteRequest request) {
        int pk = id.intValue();
        quoteRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada con id: " + id));

        Quote quote = toEntity(request);
        quote.setId(pk);
        Quote updated = quoteRepository.update(quote);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        quoteRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada con id: " + id));
        quoteRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return quoteRepository.existsById(id.intValue());
    }

    private Quote toEntity(QuoteRequest request) {
        Quote quote = new Quote();
        quote.setStatus(request.getStatus());
        quote.setTotalAmount(request.getTotalAmount());
        quote.setValidUntil(request.getValidUntil());
        quote.setDescription(request.getDescription());
        quote.setClient(new Account(request.getClientId()));
        quote.setOfferedService(new OfferedService(request.getOfferedServiceId()));
        return quote;
    }

    private QuoteResponse toResponse(Quote quote) {
        QuoteResponse.QuoteResponseBuilder builder = QuoteResponse.builder()
                .id(quote.getId())
                .status(quote.getStatus())
                .totalAmount(quote.getTotalAmount())
                .validUntil(quote.getValidUntil())
                .description(quote.getDescription())
                .createdAt(quote.getCreatedAt());

        if (quote.getClient() != null) {
            builder.client(AccountRef.builder()
                    .idUser(quote.getClient().getIdUser())
                    .name(quote.getClient().getName())
                    .lastName(quote.getClient().getLastName())
                    .email(quote.getClient().getEmail())
                    .build());
        }

        if (quote.getOfferedService() != null) {
            builder.offeredService(OfferedServiceRef.builder()
                    .id(quote.getOfferedService().getId())
                    .title(quote.getOfferedService().getTitle())
                    .description(quote.getOfferedService().getDescription())
                    .basePrice(quote.getOfferedService().getBasePrice())
                    .deliveryTimeDays(quote.getOfferedService().getDeliveryTimeDays())
                    .createdAt(quote.getOfferedService().getCreatedAt())
                    .updatedAt(quote.getOfferedService().getUpdatedAt())
                    .build());
        }

        return builder.build();
    }
}
