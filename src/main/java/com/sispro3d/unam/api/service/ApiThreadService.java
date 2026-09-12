package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.ThreadRequestDTO;
import com.sispro3d.unam.api.dto.ThreadResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.exception.InvalidRequestException;
import com.sispro3d.unam.api.mapper.ApiThreadMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.thread.repository.ThreadRepository;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import com.sispro3d.unam.message.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiThreadService {

    private final ThreadRepository threadRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AccountRepository accountRepository;
    private final MessageRepository messageRepository;
    private final ApiThreadMapper mapper;

    public ApiThreadService(ThreadRepository threadRepository,
                            WorkOrderRepository workOrderRepository,
                            AccountRepository accountRepository,
                            MessageRepository messageRepository,
                            ApiThreadMapper mapper) {
        this.threadRepository = threadRepository;
        this.workOrderRepository = workOrderRepository;
        this.accountRepository = accountRepository;
        this.messageRepository = messageRepository;
        this.mapper = mapper;
    }

    public ThreadResponseDTO create(Long workOrderId, ThreadRequestDTO request) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", workOrderId));

        var actor = accountRepository.findById(request.getActorId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", request.getActorId()));

        requireParticipant(order, actor.getIdUser());

        if (threadRepository.existsByWorkOrder_Id(order.getId())) {
            throw new DataIntegrityException("A work order can have only one thread");
        }

        Thread thread = new Thread();
        thread.setWorkOrder(order);

        return mapper.toResponse(threadRepository.save(thread));
    }

    public ThreadResponseDTO findByWorkOrderId(Long workOrderId) {
        if (!workOrderRepository.existsById(workOrderId)) {
            throw ResourceNotFoundException.forId("WorkOrder", workOrderId);
        }
        return threadRepository.findByWorkOrder_Id(workOrderId)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("Thread", workOrderId));
    }

    public ThreadResponseDTO findById(Long id) {
        return threadRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("Thread", id));
    }

    @Transactional
    public void delete(Long id) {
        if (!threadRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Thread", id);
        }
        messageRepository.deleteByThread_Id(id);
        threadRepository.deleteById(id);
    }

    private void requireParticipant(WorkOrder order, Long userId) {
        var client = order.getQuote().getClient();
        var expert = order.getQuote().getOfferedService().getExpert();

        boolean isClient = client != null && userId.equals(client.getIdUser());
        boolean isExpert = expert != null && userId.equals(expert.getIdUser());

        if (!isClient && !isExpert) {
            throw new InvalidRequestException("Only the order's client or expert can access the thread");
        }
    }
}