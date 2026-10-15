package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.ThreadRequestDTO;
import com.sispro3d.unam.api.dto.ThreadResponseDTO;
import com.sispro3d.unam.api.exception.DataIntegrityException;
import com.sispro3d.unam.api.exception.InvalidRequestException;
import com.sispro3d.unam.api.mapper.ApiThreadMapper;
import com.sispro3d.unam.core.exception.ConflictException;
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
    private final ApiOrderAccess access;

    public ApiThreadService(ThreadRepository threadRepository,
                            WorkOrderRepository workOrderRepository,
                            AccountRepository accountRepository,
                            MessageRepository messageRepository,
                            ApiThreadMapper mapper,
                            ApiOrderAccess access) {
        this.threadRepository = threadRepository;
        this.workOrderRepository = workOrderRepository;
        this.accountRepository = accountRepository;
        this.messageRepository = messageRepository;
        this.mapper = mapper;
        this.access = access;
    }

    public ThreadResponseDTO create(Long workOrderId, ThreadRequestDTO request) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", workOrderId));

        access.requireParticipant(order);

        if (threadRepository.existsByWorkOrder_Id(order.getId())) {
            throw new ConflictException("A work order can have only one thread");
        }

        Thread thread = new Thread();
        thread.setWorkOrder(order);

        return mapper.toResponse(threadRepository.save(thread));
    }

    public ThreadResponseDTO findByWorkOrderId(Long workOrderId) {
        var order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", workOrderId));

        access.requireParticipant(order);

        return threadRepository.findByWorkOrder_Id(workOrderId)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("Thread", workOrderId));
    }

    public ThreadResponseDTO findById(Long id) {
        return mapper.toResponse(findThread(id));
    }

    @Transactional
    public void delete(Long id) {
        var thread = findThread(id);
        access.requireParticipant(thread.getWorkOrder());

        messageRepository.deleteByThread_Id(id);
        threadRepository.delete(thread);
    }

    private Thread findThread(Long id) {
        var thread = threadRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Thread", id));
        access.requireParticipant(thread.getWorkOrder());
        return thread;
    }
}