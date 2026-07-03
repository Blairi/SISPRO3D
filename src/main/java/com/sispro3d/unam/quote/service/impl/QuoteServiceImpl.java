package com.sispro3d.unam.quote.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.offeredservice.domain.OfferedService;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceDTO;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.quote.dto.QuoteDTO;
import com.sispro3d.unam.quote.service.QuoteService;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.domain.Client;
import com.sispro3d.unam.user.dto.AccountDTO;
import com.sispro3d.unam.user.dto.ClientDTO;

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

        if (dto.getClient() != null && dto.getClient().getAccount() != null) {
            Client client = new Client();
            client.setAccount(new Account(dto.getClient().getAccount().getIdUser()));
            quote.setClient(client);
        }

        if (dto.getOfferedService() != null) {
            quote.setOfferedService(mapOfferedServiceByDTO(dto.getOfferedService()));
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

        if (quote.getClient() != null) {
            ClientDTO clientDTO = new ClientDTO();
            if (quote.getClient().getAccount() != null) {
                clientDTO.setAccount(mapAccountToDTO(quote.getClient().getAccount()));
            }
            dto.setClient(clientDTO);
        }

        if (quote.getOfferedService() != null) {
            dto.setOfferedService(mapOfferedServiceToDTO(quote.getOfferedService()));
        }

        return dto;
    }

    private OfferedService mapOfferedServiceByDTO(OfferedServiceDTO offeredServiceDTO) {
        OfferedService offeredService = new OfferedService();
        offeredService.setId(offeredServiceDTO.getId());
        offeredService.setTitle(offeredServiceDTO.getTitle());
        offeredService.setDescription(offeredServiceDTO.getDescription());
        offeredService.setBasePrice(offeredServiceDTO.getBasePrice());
        offeredService.setDeliveryTimeDays(offeredServiceDTO.getDeliveryTimeDays());
        return offeredService;
    }

    private OfferedServiceDTO mapOfferedServiceToDTO(OfferedService offeredService) {
        OfferedServiceDTO dto = new OfferedServiceDTO();
        dto.setId(offeredService.getId());
        dto.setTitle(offeredService.getTitle());
        dto.setDescription(offeredService.getDescription());
        dto.setBasePrice(offeredService.getBasePrice());
        dto.setDeliveryTimeDays(offeredService.getDeliveryTimeDays());
        dto.setCreatedAt(offeredService.getCreatedAt());
        dto.setUpdatedAt(offeredService.getUpdatedAt());
        return dto;
    }

    private AccountDTO mapAccountToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setIdUser(account.getIdUser());
        dto.setName(account.getName());
        dto.setLastName(account.getLastName());
        dto.setEmail(account.getEmail());
        dto.setPhone(account.getPhone());
        dto.setPassword(account.getPassword());
        dto.setType(account.getType());
        dto.setCreatedAt(account.getCreatedAt());
        return dto;
    }
}
