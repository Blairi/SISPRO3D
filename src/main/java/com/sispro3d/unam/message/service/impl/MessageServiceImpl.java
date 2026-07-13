package com.sispro3d.unam.message.service.impl;

import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.ThreadRef;
import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.message.dto.MessageRequest;
import com.sispro3d.unam.message.dto.MessageResponse;
import com.sispro3d.unam.message.repository.MessageRepository;
import com.sispro3d.unam.message.service.MessageService;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.user.domain.Account;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public List<MessageResponse> findAll() {
        return messageRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<MessageResponse> findById(Long id) {
        return messageRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public MessageResponse create(MessageRequest request) {
        Message message = toEntity(request);
        Message saved = messageRepository.save(message);
        return toResponse(saved);
    }

    @Override
    public MessageResponse update(Long id, MessageRequest request) {
        int pk = id.intValue();
        messageRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado con id: " + id));

        Message message = toEntity(request);
        message.setId(pk);
        Message updated = messageRepository.update(message);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        messageRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado con id: " + id));
        messageRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return messageRepository.existsById(id.intValue());
    }

    private Message toEntity(MessageRequest request) {
        Message message = new Message();
        message.setContent(request.getContent());
        message.setThread(new Thread(request.getThreadId()));
        message.setAccount(new Account(request.getAccountId()));
        return message;
    }

    private MessageResponse toResponse(Message message) {
        MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .timeStamp(message.getTimeStamp());

        if (message.getThread() != null) {
            builder.thread(ThreadRef.builder()
                    .id(message.getThread().getId())
                    .build());
        }

        if (message.getAccount() != null) {
            builder.account(AccountRef.builder()
                    .idUser(message.getAccount().getIdUser())
                    .name(message.getAccount().getName())
                    .lastName(message.getAccount().getLastName())
                    .email(message.getAccount().getEmail())
                    .phone(message.getAccount().getPhone())
                    .role(message.getAccount().getRole())
                    .build());
        }

        return builder.build();
    }
}
