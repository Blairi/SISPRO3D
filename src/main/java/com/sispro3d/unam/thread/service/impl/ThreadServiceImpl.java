package com.sispro3d.unam.thread.service.impl;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.thread.domain.Thread;
import com.sispro3d.unam.thread.dto.ThreadRequest;
import com.sispro3d.unam.thread.dto.ThreadResponse;
import com.sispro3d.unam.thread.mapper.ThreadMapper;
import com.sispro3d.unam.thread.repository.ThreadRepository;
import com.sispro3d.unam.thread.service.ThreadService;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ThreadServiceImpl implements ThreadService {

    private final ThreadRepository threadRepository;
    private final ThreadMapper threadMapper;
    private final WorkOrderRepository workOrderRepository;
    private final AccountRepository accountRepository;

    public ThreadServiceImpl(ThreadRepository threadRepository,
                             ThreadMapper threadMapper,
                             WorkOrderRepository workOrderRepository,
                             AccountRepository accountRepository) {
        this.threadRepository = threadRepository;
        this.threadMapper = threadMapper;
        this.workOrderRepository = workOrderRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<ThreadResponse> findAll() {
        return threadRepository.findAll().stream()
                .map(threadMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<ThreadResponse> findById(Long id) {
        return threadRepository.findById(id)
                .map(threadMapper::toResponse);
    }

    @Override
    public ThreadResponse create(ThreadRequest request) {
        WorkOrder order = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", request.getWorkOrderId()));

        var actor = accountRepository.findById(request.getActorId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account", request.getActorId()));

        requireParticipant(order, actor.getIdUser());

        if (threadRepository.existsByWorkOrder_Id(order.getId())) {
            throw new IllegalStateException("Solo puede existir un hilo de conversación por orden");
        }

        Thread thread = threadMapper.toEntity(request);
        thread.setWorkOrder(order);

        Thread saved = threadRepository.save(thread);
        return threadMapper.toResponse(saved);
    }

    @Override
    public ThreadResponse update(Long id, ThreadRequest request) {
        Thread existing = threadRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Thread", id));

        if (!workOrderRepository.existsById(request.getWorkOrderId())) {
            throw ResourceNotFoundException.forId("WorkOrder", request.getWorkOrderId());
        }

        existing.setWorkOrder(workOrderRepository.getReferenceById(request.getWorkOrderId()));
        Thread updated = threadRepository.save(existing);
        return threadMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!threadRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Thread", id);
        }
        threadRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return threadRepository.existsById(id);
    }

    private void requireParticipant(WorkOrder order, Long userId) {
        var client = order.getQuote().getClient();
        var expert = order.getQuote().getOfferedService().getExpert();

        boolean isClient = client != null && userId.equals(client.getIdUser());
        boolean isExpert = expert != null && userId.equals(expert.getIdUser());

        if (!isClient && !isExpert) {
            throw new IllegalStateException("Solo el cliente o el experto de la orden pueden acceder al hilo");
        }
    }

    @Override
    public Optional<ThreadResponse> findByWorkOrderId(Long workOrderId) {
        return threadRepository.findByWorkOrder_Id(workOrderId)
                .map(threadMapper::toResponse);
    }
}
