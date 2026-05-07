package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.QuoteDTO;
import mx.unam.dgtic.entities.QuoteEntity;
import mx.unam.dgtic.mapper.QuoteMapper;
import mx.unam.dgtic.repository.IQuoteRepository;
import mx.unam.dgtic.repository.impl.QuoteRepository;
import mx.unam.dgtic.service.QuoteService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class QuoteServiceImpl implements QuoteService {
    private final IQuoteRepository quoteRepository;

    public QuoteServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.quoteRepository = new QuoteRepository(em);
    }

    @Override
    public List<QuoteDTO> findAll() {
        return QuoteMapper.toDtoList(quoteRepository.findAll());
    }

    @Override
    public QuoteDTO findById(Integer id) {
        return QuoteMapper.toDTO(quoteRepository.findById(id));
    }

    @Override
    public QuoteDTO create(QuoteDTO dto) {
        QuoteEntity entity = QuoteMapper.toEntity(dto);
        quoteRepository.save(entity);
        return QuoteMapper.toDTO(entity);
    }

    @Override
    public QuoteDTO update(Integer id, QuoteDTO dto) {
        QuoteEntity existingQuote = quoteRepository.findById(id);
        if (existingQuote == null) {
            throw new RuntimeException("Cotización no encontrada con id: " + id);
        }

        QuoteEntity quoteEntity = QuoteMapper.toEntity(dto);
        quoteEntity.setId(id);
        quoteRepository.update(quoteEntity);
        return QuoteMapper.toDTO(quoteEntity);
    }

    @Override
    public void delete(Integer id) {
        QuoteEntity existingQuote = quoteRepository.findById(id);
        if (existingQuote == null) {
            throw new RuntimeException("Cotización no encontrada con id: " + id);
        }
        quoteRepository.delete(existingQuote);
    }
}
