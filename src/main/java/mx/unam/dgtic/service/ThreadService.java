package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.ThreadDTO;

import java.util.List;
import java.util.Optional;

public interface ThreadService {
    List<ThreadDTO> findAll();
    Optional<ThreadDTO> findById(int id);
    ThreadDTO create(ThreadDTO dto);
    ThreadDTO update(int id, ThreadDTO dto);
    void delete(int id);
}
