package com.sispro3d.unam.message.service.impl;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.message.dto.MessageRequest;
import com.sispro3d.unam.message.dto.MessageResponse;
import com.sispro3d.unam.message.mapper.MessageMapper;
import com.sispro3d.unam.message.repository.MessageRepository;
import com.sispro3d.unam.message.service.MessageService;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.thread.repository.ThreadRepository;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private ThreadRepository threadRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public List<MessageResponse> findAll() {
        return messageRepository.findAll().stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<MessageResponse> findById(Long id) {
        return messageRepository.findById(id)
                .map(messageMapper::toResponse);
    }

    @Override
    public MessageResponse create(MessageRequest request) {
        Thread thread = threadRepository.findById(request.getThreadId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Thread", request.getThreadId()));

        var user = accountRepository.findById(request.getUserId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", request.getUserId()));

        requireParticipant(thread.getWorkOrder(), user.getIdUser());

        Message message = messageMapper.toEntity(request);
        message.setThread(thread);
        message.setAccount(user);
        message.setTimestamp(LocalDateTime.now());

        Message saved = messageRepository.save(message);
        return messageMapper.toResponse(saved);
    }

    @Override
    public MessageResponse update(Long id, MessageRequest request) {
        Message existing = messageRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Message", id));

        if (!threadRepository.existsById(request.getThreadId())) {
            throw ResourceNotFoundException.forId("Thread", request.getThreadId());
        }
        if (!accountRepository.existsById(request.getUserId())) {
            throw ResourceNotFoundException.forId("Account", request.getUserId());
        }

        messageMapper.updateEntityFromRequest(request, existing);
        existing.setThread(threadRepository.getReferenceById(request.getThreadId()));
        existing.setAccount(accountRepository.getReferenceById(request.getUserId()));
        Message updated = messageRepository.save(existing);
        return messageMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!messageRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Message", id);
        }
        messageRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return messageRepository.existsById(id);
    }

    private void requireParticipant(WorkOrder order, Long userId) {
        var client = order.getQuote().getClient();
        var expert = order.getQuote().getOfferedService().getExpert();

        boolean isClient = client != null && userId.equals(client.getIdUser());
        boolean isExpert = expert != null && userId.equals(expert.getIdUser());

        if (!isClient && !isExpert) {
            throw new IllegalStateException("Solo el cliente o el experto de la orden pueden enviar mensajes");
        }
    }

    @Override
    public List<MessageResponse> findByThreadId(Long threadId) {
        return messageRepository.findByThread_IdOrderByTimestampAsc(threadId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }
}
