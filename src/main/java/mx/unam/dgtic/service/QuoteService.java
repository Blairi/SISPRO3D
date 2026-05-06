package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.QuoteDTO;

import java.util.List;
import java.util.Optional;

public interface QuoteService {
    List<QuoteDTO> findAll();
    Optional<QuoteDTO> findById(int id);
    QuoteDTO create(QuoteDTO dto);
    QuoteDTO update(int id, QuoteDTO dto);
    void delete(int id);
}
