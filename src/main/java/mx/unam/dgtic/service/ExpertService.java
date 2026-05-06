package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.ExpertDTO;

import java.util.List;
import java.util.Optional;

public interface ExpertService {
    List<ExpertDTO> findAll();
    Optional<ExpertDTO> findById(int id);
    ExpertDTO create(ExpertDTO dto);
    ExpertDTO update(int id, ExpertDTO dto);
    void delete(int id);
}
