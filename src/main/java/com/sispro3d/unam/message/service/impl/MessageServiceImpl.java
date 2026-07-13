package com.sispro3d.unam.message.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.ThreadRef;
import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.message.dto.MessageDTO;
import com.sispro3d.unam.message.service.MessageService;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.user.domain.Account;

import java.util.List;
import java.util.Optional;

public class MessageServiceImpl implements MessageService {

    private final GenericDAO<Message> messageDAO;

    public MessageServiceImpl(GenericDAO<Message> messageDAO) {
        this.messageDAO = messageDAO;
    }

    @Override
    public List<MessageDTO> findAll() {
        return messageDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<MessageDTO> findById(int id) {
        return messageDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public MessageDTO create(MessageDTO dto) {
        Message message = toEntity(dto);
        int generatedId = messageDAO.insert(message);
        message.setId(generatedId);
        return toResponseDTO(message);
    }

    @Override
    public MessageDTO update(int id, MessageDTO dto) {
        messageDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado con id: " + id));

        Message message = toEntity(dto);
        message.setId(id);
        messageDAO.update(message);
        return toResponseDTO(message);
    }

    @Override
    public void delete(int id) {
        messageDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado con id: " + id));
        messageDAO.delete(id);
    }

    private Message toEntity(MessageDTO dto) {
        Message message = new Message();
        message.setContent(dto.getContent());

        if (dto.getThread() != null) {
            Thread thread = new Thread(dto.getThread().getId());
            message.setThread(thread);
        }

        if (dto.getAccount() != null) {
            Account account = new Account(dto.getAccount().getIdUser());
            message.setAccount(account);
        }

        return message;
    }

    private MessageDTO toResponseDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setTimeStamp(message.getTimeStamp());

        if (message.getThread() != null) {
            ThreadRef threadRef = ThreadRef.builder()
                    .id(message.getThread().getId())
                    .build();
            dto.setThread(threadRef);
        }

        if (message.getAccount() != null) {
            AccountRef accountRef = AccountRef.builder()
                    .idUser(message.getAccount().getIdUser())
                    .name(message.getAccount().getName())
                    .lastName(message.getAccount().getLastName())
                    .email(message.getAccount().getEmail())
                    .phone(message.getAccount().getPhone())
                    .role(message.getAccount().getRole())
                    .build();
            dto.setAccount(accountRef);
        }

        return dto;
    }


}
