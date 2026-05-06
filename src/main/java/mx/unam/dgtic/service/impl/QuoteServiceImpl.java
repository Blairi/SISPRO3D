package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dao.GenericDAO;
import mx.unam.dgtic.domain.Quote;
import mx.unam.dgtic.dto.QuoteDTO;
import mx.unam.dgtic.dto.ClientDTO;
import mx.unam.dgtic.dto.ServiceDTO;
import mx.unam.dgtic.service.QuoteService;

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
            mx.unam.dgtic.domain.Client client = new mx.unam.dgtic.domain.Client();
            client.setAccount(new mx.unam.dgtic.domain.Account(dto.getClient().getAccount().getIdUser()));
            quote.setClient(client);
        }

        if (dto.getService() != null) {
            quote.setService(mapServiceByDTO(dto.getService()));
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

        if (quote.getService() != null) {
            dto.setService(mapServiceToDTO(quote.getService()));
        }

        return dto;
    }

    private mx.unam.dgtic.domain.Service mapServiceByDTO(ServiceDTO serviceDTO) {
        mx.unam.dgtic.domain.Service service = new mx.unam.dgtic.domain.Service();
        service.setId(serviceDTO.getId());
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setBasePrice(serviceDTO.getBasePrice());
        service.setDeliveryTimeDays(serviceDTO.getDeliveryTimeDays());
        return service;
    }

    private ServiceDTO mapServiceToDTO(mx.unam.dgtic.domain.Service service) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setTitle(service.getTitle());
        dto.setDescription(service.getDescription());
        dto.setBasePrice(service.getBasePrice());
        dto.setDeliveryTimeDays(service.getDeliveryTimeDays());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }

    private mx.unam.dgtic.dto.AccountDTO mapAccountToDTO(mx.unam.dgtic.domain.Account account) {
        mx.unam.dgtic.dto.AccountDTO dto = new mx.unam.dgtic.dto.AccountDTO();
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
