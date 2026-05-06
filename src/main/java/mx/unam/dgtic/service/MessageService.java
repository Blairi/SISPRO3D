package mx.unam.dgtic.service;

import mx.unam.dgtic.dto.MessageDTO;

import java.util.List;
import java.util.Optional;

public interface MessageService {
    List<MessageDTO> findAll();
    Optional<MessageDTO> findById(int id);
    MessageDTO create(MessageDTO dto);
    MessageDTO update(int id, MessageDTO dto);
    void delete(int id);
}
