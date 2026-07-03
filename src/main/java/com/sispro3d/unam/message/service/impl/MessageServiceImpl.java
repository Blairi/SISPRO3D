package com.sispro3d.unam.message.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.message.dto.MessageDTO;
import com.sispro3d.unam.message.service.MessageService;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.thread.dto.ThreadDTO;
import com.sispro3d.unam.user.domain.Account;
import com.sispro3d.unam.user.dto.AccountDTO;
import com.sispro3d.unam.message.service.MessageService;

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
            ThreadDTO threadDTO = new ThreadDTO();
            threadDTO.setId(message.getThread().getId());
            dto.setThread(threadDTO);
        }

        if (message.getAccount() != null) {
            AccountDTO accountDTO = mapAccountToDTO(message.getAccount());
            dto.setAccount(accountDTO);
        }

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
