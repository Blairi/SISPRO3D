package com.sispro3d.unam.deliverable.service.impl;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import com.sispro3d.unam.deliverable.mapper.DeliverableMapper;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeliverableServiceImpl implements DeliverableService {

    @Autowired
    private DeliverableRepository deliverableRepository;
    @Autowired
    private DeliverableMapper deliverableMapper;
    @Autowired
    private WorkOrderRepository workOrderRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public List<DeliverableResponse> findAll() {
        return deliverableRepository.findAll().stream()
                .map(deliverableMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<DeliverableResponse> findById(Long id) {
        return deliverableRepository.findById(id)
                .map(deliverableMapper::toResponse);
    }

    @Override
    public DeliverableResponse create(DeliverableRequest request) {
        WorkOrder order = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", request.getWorkOrderId()));

        requireExpertOwner(order, request.getExpertId());
        requireActiveOrder(order);

        Deliverable deliverable = deliverableMapper.toEntity(request);
        deliverable.setWorkOrder(order);
        deliverable.setCreatedAt(LocalDateTime.now());

        Deliverable saved = deliverableRepository.save(deliverable);
        return deliverableMapper.toResponse(saved);
    }

    @Override
    public DeliverableResponse update(Long id, DeliverableRequest request) {
        Deliverable existing = deliverableRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Deliverable", id));

        if (!workOrderRepository.existsById(request.getWorkOrderId())) {
            throw ResourceNotFoundException.forId("WorkOrder", request.getWorkOrderId());
        }

        deliverableMapper.updateEntityFromRequest(request, existing);
        existing.setWorkOrder(workOrderRepository.getReferenceById(request.getWorkOrderId()));
        Deliverable updated = deliverableRepository.save(existing);
        return deliverableMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!deliverableRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Deliverable", id);
        }
        deliverableRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return deliverableRepository.existsById(id);
    }

    private void requireExpertOwner(WorkOrder order, Long expertId) {
        var expert = accountRepository.findById(expertId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", expertId));

        if (expert.getRole() != Role.EXPERT) {
            throw new IllegalStateException("Solo cuentas de tipo EXPERT pueden gestionar entregables");
        }

        var serviceExpert = order.getQuote().getOfferedService().getExpert();
        if (serviceExpert == null || !expertId.equals(serviceExpert.getIdUser())) {
            throw new IllegalStateException("Solo el experto propietario del servicio puede gestionar la orden");
        }
    }

    private void requireActiveOrder(WorkOrder order) {
        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS
                && order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            throw new IllegalStateException("Solo se pueden gestionar entregables en órdenes en estado IN_PROGRESS o IN_REVIEW");
        }
    }

    @Override
    public List<DeliverableResponse> findByWorkOrderId(Long workOrderId) {
        return deliverableRepository.findByWorkOrder_Id(workOrderId).stream()
                .map(deliverableMapper::toResponse)
                .toList();
    }
}
