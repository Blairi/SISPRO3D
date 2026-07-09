package com.sispro3d.unam.quote.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.core.dto.ClientRef;
import com.sispro3d.unam.core.dto.OfferedServiceRef;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.dto.QuoteDTO;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Client;

import java.util.List;
import java.util.Optional;

public class QuoteServiceImpl implements QuoteService {

    private final GenericDAO<Quote> quoteDAO;

    public QuoteServiceImpl(GenericDAO<Quote> quoteDAO) {
        this.quoteDAO = quoteDAO;
    }

    @Override
    public List<QuoteDTO> findAll() {
        return quoteDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<QuoteDTO> findById(int id) {
        return quoteDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public QuoteDTO create(QuoteDTO dto) {
        Quote quote = toEntity(dto);
        int generatedId = quoteDAO.insert(quote);
        quote.setId(generatedId);
        return toResponseDTO(quote);
    }

    @Override
    public QuoteDTO update(int id, QuoteDTO dto) {
        quoteDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada con id: " + id));

        Quote quote = toEntity(dto);
        quote.setId(id);
        quoteDAO.update(quote);
        return toResponseDTO(quote);
    }

    @Override
    public void delete(int id) {
        quoteDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada con id: " + id));
        quoteDAO.delete(id);
    }

    private Quote toEntity(QuoteDTO dto) {
        Quote quote = new Quote();
        quote.setStatus(dto.getStatus());
        quote.setTotalAmount(dto.getTotalAmount());
        quote.setValidUntil(dto.getValidUntil());
        quote.setDescription(dto.getDescription());

        if (dto.getClient() != null) {
            Client client = new Client();
            client.setAccount(new Account(dto.getClient().getId()));
            quote.setClient(client);
        }

        if (dto.getOfferedService() != null) {
            quote.setOfferedService(mapOfferedServiceByRef(dto.getOfferedService()));
        }

        return quote;
    }

    private QuoteDTO toResponseDTO(Quote quote) {
        QuoteDTO dto = new QuoteDTO();
        dto.setId(quote.getId());
        dto.setStatus(quote.getStatus());
        dto.setTotalAmount(quote.getTotalAmount());
        dto.setValidUntil(quote.getValidUntil());
        dto.setDescription(quote.getDescription());
        dto.setCreatedAt(quote.getCreatedAt());

        if (quote.getClient() != null && quote.getClient().getAccount() != null) {
            ClientRef clientRef = ClientRef.builder()
                    .id(quote.getClient().getAccount().getIdUser())
                    .name(quote.getClient().getAccount().getName())
                    .lastName(quote.getClient().getAccount().getLastName())
                    .email(quote.getClient().getAccount().getEmail())
                    .build();
            dto.setClient(clientRef);
        }

        if (quote.getOfferedService() != null) {
            dto.setOfferedService(mapOfferedServiceToRef(quote.getOfferedService()));
        }

        return dto;
    }

    private OfferedService mapOfferedServiceByRef(OfferedServiceRef ref) {
        OfferedService offeredService = new OfferedService();
        offeredService.setId(ref.getId());
        offeredService.setTitle(ref.getTitle());
        offeredService.setDescription(ref.getDescription());
        offeredService.setBasePrice(ref.getBasePrice());
        offeredService.setDeliveryTimeDays(ref.getDeliveryTimeDays());
        return offeredService;
    }

    private OfferedServiceRef mapOfferedServiceToRef(OfferedService offeredService) {
        return OfferedServiceRef.builder()
                .id(offeredService.getId())
                .title(offeredService.getTitle())
                .description(offeredService.getDescription())
                .basePrice(offeredService.getBasePrice())
                .deliveryTimeDays(offeredService.getDeliveryTimeDays())
                .createdAt(offeredService.getCreatedAt())
                .updatedAt(offeredService.getUpdatedAt())
                .build();
    }
}
