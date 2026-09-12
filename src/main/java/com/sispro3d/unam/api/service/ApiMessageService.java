package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.MessageRequestDTO;
import com.sispro3d.unam.api.dto.MessageResponseDTO;
import com.sispro3d.unam.api.exception.InvalidRequestException;
import com.sispro3d.unam.api.mapper.ApiMessageMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.message.repository.MessageRepository;
import com.sispro3d.unam.thread.repository.ThreadRepository;
import com.sispro3d.unam.user.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiMessageService {

    private final MessageRepository messageRepository;
    private final ThreadRepository threadRepository;
    private final AccountRepository accountRepository;
    private final ApiMessageMapper mapper;

    public ApiMessageService(MessageRepository messageRepository,
                             ThreadRepository threadRepository,
                             AccountRepository accountRepository,
                             ApiMessageMapper mapper) {
        this.messageRepository = messageRepository;
        this.threadRepository = threadRepository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    public MessageResponseDTO post(Long threadId, MessageRequestDTO request) {
        var thread = threadRepository.findById(threadId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Thread", threadId));

        var author = accountRepository.findById(request.getAuthorId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", request.getAuthorId()));

        requireParticipant(thread, author.getIdUser());

        Message message = new Message();
        message.setThread(thread);
        message.setAccount(author);
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now());

        return mapper.toResponse(messageRepository.save(message));
    }

    public List<MessageResponseDTO> findByThreadId(Long threadId) {
        if (!threadRepository.existsById(threadId)) {
            throw ResourceNotFoundException.forId("Thread", threadId);
        }
        return messageRepository.findByThread_IdOrderByTimestampAsc(threadId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public MessageResponseDTO findById(Long id) {
        return messageRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("Message", id));
    }

    public void delete(Long id) {
        if (!messageRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Message", id);
        }
        messageRepository.deleteById(id);
    }

    private void requireParticipant(com.sispro3d.unam.thread.domain.Thread thread, Long userId) {
        var order = thread.getWorkOrder();
        var client = order.getQuote().getClient();
        var expert = order.getQuote().getOfferedService().getExpert();

        boolean isClient = client != null && userId.equals(client.getIdUser());
        boolean isExpert = expert != null && userId.equals(expert.getIdUser());

        if (!isClient && !isExpert) {
            throw new InvalidRequestException("Only the order's client or expert can post messages");
        }
    }
}