package com.sispro3d.unam.workorder.service.impl;

import com.sispro3d.unam.core.dto.QuoteRef;
import com.sispro3d.unam.quote.domain.Quote;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;

    public WorkOrderServiceImpl(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    @Override
    public List<WorkOrderResponse> findAll() {
        return workOrderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<WorkOrderResponse> findById(Long id) {
        return workOrderRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public WorkOrderResponse create(WorkOrderRequest request) {
        WorkOrder workOrder = toEntity(request);
        WorkOrder saved = workOrderRepository.save(workOrder);
        return toResponse(saved);
    }

    @Override
    public WorkOrderResponse update(Long id, WorkOrderRequest request) {
        int pk = id.intValue();
        workOrderRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Orden de trabajo no encontrada con id: " + id));

        WorkOrder workOrder = toEntity(request);
        workOrder.setId(pk);
        WorkOrder updated = workOrderRepository.update(workOrder);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        workOrderRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Orden de trabajo no encontrada con id: " + id));
        workOrderRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return workOrderRepository.existsById(id.intValue());
    }

    private WorkOrder toEntity(WorkOrderRequest request) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setStatus(request.getStatus());
        workOrder.setStartedAt(request.getStartedAt());
        workOrder.setCompletedAt(request.getCompletedAt());

        if (request.getQuoteId() != null) {
            workOrder.setQuote(new Quote(request.getQuoteId()));
        }

        return workOrder;
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder) {
        WorkOrderResponse.WorkOrderResponseBuilder builder = WorkOrderResponse.builder()
                .id(workOrder.getId())
                .status(workOrder.getStatus())
                .startedAt(workOrder.getStartedAt())
                .completedAt(workOrder.getCompletedAt())
                .createdAt(workOrder.getCreatedAt());

        if (workOrder.getQuote() != null) {
            builder.quote(QuoteRef.builder()
                    .id(workOrder.getQuote().getId())
                    .status(workOrder.getQuote().getStatus())
                    .totalAmount(workOrder.getQuote().getTotalAmount())
                    .validUntil(workOrder.getQuote().getValidUntil())
                    .description(workOrder.getQuote().getDescription())
                    .createdAt(workOrder.getQuote().getCreatedAt())
                    .build());
        }

        return builder.build();
    }
}
