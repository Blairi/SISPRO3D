package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.PreviewDTO;

import java.util.List;
import java.util.Optional;

public interface PreviewService {
    List<PreviewDTO> findAll();
    Optional<PreviewDTO> findById(int id);
    PreviewDTO create(PreviewDTO dto);
    PreviewDTO update(int id, PreviewDTO dto);
    void delete(int id);
}
