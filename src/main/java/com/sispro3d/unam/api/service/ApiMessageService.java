package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.MessageRequestDTO;
import com.sispro3d.unam.api.dto.MessageResponseDTO;
import com.sispro3d.unam.api.exception.InvalidRequestException;
import com.sispro3d.unam.api.mapper.ApiMessageMapper;
import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;

import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.message.repository.MessageRepository;
import com.sispro3d.unam.thread.repository.ThreadRepository;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
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
    private final ApiOrderAccess access;

    public ApiMessageService(MessageRepository messageRepository,
                             ThreadRepository threadRepository,
                             AccountRepository accountRepository,
                             ApiMessageMapper mapper,
                            ApiOrderAccess access) {
        this.messageRepository = messageRepository;
        this.threadRepository = threadRepository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
        this.access = access;
    }

    public MessageResponseDTO post(Long threadId, MessageRequestDTO request) {
        var thread = threadRepository.findById(threadId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Thread", threadId));

        access.requireParticipant(thread.getWorkOrder());

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("A message cannot be blank");
        }

        WorkOrderStatus status = thread.getWorkOrder().getStatus();
        if (status == WorkOrderStatus.COMPLETED || status == WorkOrderStatus.CANCELED) {
            throw new BadRequestException("No messages can be posted to a concluded order");
        }

        Long authorId = access.currentAccountId();
        var author = accountRepository.findById(authorId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", authorId));

        Message message = new Message();
        message.setThread(thread);
        message.setAccount(author);
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now());

        return mapper.toResponse(messageRepository.save(message));
    }

    public List<MessageResponseDTO> findByThreadId(Long threadId) {
        var thread = threadRepository.findById(threadId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Thread", threadId));

        access.requireParticipant(thread.getWorkOrder());

        return messageRepository.findByThread_IdOrderByTimestampAsc(threadId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public MessageResponseDTO findById(Long id) {
        return mapper.toResponse(findMessage(id));
    }

    public void delete(Long id) {
        var message = findMessage(id);
        access.requireParticipant(message.getThread().getWorkOrder());
        messageRepository.delete(message);
    }

    private Message findMessage(Long id) {
        var message = messageRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Message", id));
        access.requireParticipant(message.getThread().getWorkOrder());
        return message;
    }
}