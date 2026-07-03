package com.sispro3d.unam.service;

import com.sispro3d.unam.dto.MessageDTO;

import java.util.List;
import java.util.Optional;

public interface MessageService {
    List<MessageDTO> findAll();
    Optional<MessageDTO> findById(int id);
    MessageDTO create(MessageDTO dto);
    MessageDTO update(int id, MessageDTO dto);
    void delete(int id);
}
