package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.WorkOrderResponseDTO;
import com.sispro3d.unam.api.exception.InvalidRequestException;
import com.sispro3d.unam.api.mapper.ApiWorkOrderMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

@Service
public class ApiWorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final ApiWorkOrderMapper mapper;

    public ApiWorkOrderService(WorkOrderRepository workOrderRepository,
                               ApiWorkOrderMapper mapper) {
        this.workOrderRepository = workOrderRepository;
        this.mapper = mapper;
    }

    public WorkOrderResponseDTO findById(Long id) {
        return workOrderRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));
    }

    public WorkOrderResponseDTO updateStatus(Long id, WorkOrderStatus newStatus) {
        var order = workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", id));

        validateTransition(order, newStatus);

        order.setStatus(newStatus);
        if (newStatus == WorkOrderStatus.IN_PROGRESS && order.getStartedAt() == null) {
            order.setStartedAt(java.time.LocalDateTime.now());
        }
        if (newStatus == WorkOrderStatus.COMPLETED && order.getCompletedAt() == null) {
            order.setCompletedAt(java.time.LocalDateTime.now());
        }
        return mapper.toResponse(workOrderRepository.save(order));
    }

    private void validateTransition(WorkOrder order, WorkOrderStatus next) {
        WorkOrderStatus current = order.getStatus();
        boolean valid =
                (current == WorkOrderStatus.PENDING && next == WorkOrderStatus.IN_PROGRESS) ||
                (current == WorkOrderStatus.PENDING && next == WorkOrderStatus.CANCELED) ||
                (current == WorkOrderStatus.IN_PROGRESS && next == WorkOrderStatus.IN_REVIEW) ||
                (current == WorkOrderStatus.IN_PROGRESS && next == WorkOrderStatus.CANCELED) ||
                (current == WorkOrderStatus.IN_REVIEW && next == WorkOrderStatus.COMPLETED) ||
                (current == WorkOrderStatus.IN_REVIEW && next == WorkOrderStatus.IN_PROGRESS);

        if (!valid) {
            throw new InvalidRequestException(
                    "Cannot transition WorkOrder status from " + current + " to " + next);
        }
    }
}
