package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.DeliverableRequestDTO;
import com.sispro3d.unam.api.dto.DeliverableResponseDTO;
import com.sispro3d.unam.api.mapper.ApiDeliverableMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiDeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PreviewRepository previewRepository;
    private final ApiDeliverableMapper mapper;

    public ApiDeliverableService(DeliverableRepository deliverableRepository,
                                 WorkOrderRepository workOrderRepository,
                                 PreviewRepository previewRepository,
                                 ApiDeliverableMapper mapper) {
        this.deliverableRepository = deliverableRepository;
        this.workOrderRepository = workOrderRepository;
        this.previewRepository = previewRepository;
        this.mapper = mapper;
    }

    public List<DeliverableResponseDTO> findByWorkOrderId(Long workOrderId) {
        if (!workOrderRepository.existsById(workOrderId)) {
            throw ResourceNotFoundException.forId("WorkOrder", workOrderId);
        }
        return deliverableRepository.findByWorkOrder_Id(workOrderId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public DeliverableResponseDTO create(Long workOrderId, DeliverableRequestDTO request) {
        var order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", workOrderId));

        var deliverable = mapper.toEntity(request);
        deliverable.setWorkOrder(order);
        deliverable.setCreatedAt(LocalDateTime.now());

        return mapper.toResponse(deliverableRepository.save(deliverable));
    }

    public DeliverableResponseDTO update(Long id, DeliverableRequestDTO request) {
        var existing = deliverableRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Deliverable", id));

        mapper.updateEntityFromRequest(request, existing);
        return mapper.toResponse(deliverableRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        var existing = deliverableRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Deliverable", id));
        previewRepository.deleteAll(previewRepository.findByDeliverable_Id(id));
        deliverableRepository.delete(existing);
    }
}
