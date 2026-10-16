package com.sispro3d.unam.message.service.impl;

import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ForbiddenException;
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
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ThreadRepository threadRepository;
    private final AccountRepository accountRepository;

    public MessageServiceImpl(MessageRepository messageRepository,
                              MessageMapper messageMapper,
                              ThreadRepository threadRepository,
                              AccountRepository accountRepository) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.threadRepository = threadRepository;
        this.accountRepository = accountRepository;
    }

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
        requireOrderActiva(thread.getWorkOrder());

        Message message = messageMapper.toEntity(request);
        message.setThread(thread);
        message.setAccount(user);
        message.setTimestamp(LocalDateTime.now());

        Message saved = messageRepository.save(message);
        log.info("Mensaje creado: id={}, hilo={}, autor={}", saved.getId(), request.getThreadId(), request.getUserId());
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
        log.info("Mensaje actualizado: id={}", updated.getId());
        return messageMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!messageRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Message", id);
        }
        messageRepository.deleteById(id);
        log.info("Mensaje eliminado: id={}", id);
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
            log.warn("Intento de enviar mensaje por no participante: orden={}, cuenta={}", order.getId(), userId);
            throw new ForbiddenException("Solo el cliente o el experto de la orden pueden enviar mensajes");
        }
    }

    private void requireOrderActiva(WorkOrder order) {
        WorkOrderStatus status = order.getStatus();
        if (status == WorkOrderStatus.COMPLETED || status == WorkOrderStatus.CANCELED) {
            log.warn("Intento de enviar mensaje en orden concluida: orden={}, estado={}", order.getId(), status);
            throw new BadRequestException("No se pueden enviar mensajes cuando la orden ha concluido");
        }
    }

    @Override
    public List<MessageResponse> findByThreadId(Long threadId) {
        return messageRepository.findByThread_IdOrderByTimestampAsc(threadId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }
}
