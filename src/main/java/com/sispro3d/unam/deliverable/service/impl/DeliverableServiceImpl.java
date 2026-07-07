package com.sispro3d.unam.deliverable.service.impl;

import com.sispro3d.unam.core.dto.WorkOrderRef;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeliverableServiceImpl implements DeliverableService {

    private final DeliverableRepository deliverableRepository;

    public DeliverableServiceImpl(DeliverableRepository deliverableRepository) {
        this.deliverableRepository = deliverableRepository;
    }

    @Override
    public List<DeliverableResponse> findAll() {
        return deliverableRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Optional<DeliverableResponse> findById(Long id) {
        return deliverableRepository.findById(id.intValue())
                .map(this::toResponse);
    }

    @Override
    public DeliverableResponse create(DeliverableRequest request) {
        Deliverable deliverable = toEntity(request);
        Deliverable saved = deliverableRepository.save(deliverable);
        return toResponse(saved);
    }

    @Override
    public DeliverableResponse update(Long id, DeliverableRequest request) {
        int pk = id.intValue();
        deliverableRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Entregable no encontrado con id: " + id));

        Deliverable deliverable = toEntity(request);
        deliverable.setId(pk);
        Deliverable updated = deliverableRepository.update(deliverable);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        int pk = id.intValue();
        deliverableRepository.findById(pk)
                .orElseThrow(() -> new RuntimeException("Entregable no encontrado con id: " + id));
        deliverableRepository.deleteById(pk);
    }

    @Override
    public boolean existsById(Long id) {
        return deliverableRepository.existsById(id.intValue());
    }

    private Deliverable toEntity(DeliverableRequest request) {
        Deliverable deliverable = new Deliverable();
        deliverable.setName(request.getName());
        deliverable.setUrlFile(request.getUrlFile());
        deliverable.setFileType(request.getFileType());
        deliverable.setWorkOrder(new WorkOrder(request.getWorkOrderId()));
        return deliverable;
    }

    private DeliverableResponse toResponse(Deliverable deliverable) {
        DeliverableResponse.DeliverableResponseBuilder builder = DeliverableResponse.builder()
                .id(deliverable.getId())
                .name(deliverable.getName())
                .urlFile(deliverable.getUrlFile())
                .fileType(deliverable.getFileType())
                .createdAt(deliverable.getCreatedAt());

        if (deliverable.getWorkOrder() != null) {
            builder.workOrder(WorkOrderRef.builder()
                    .id(deliverable.getWorkOrder().getId())
                    .status(deliverable.getWorkOrder().getStatus())
                    .startedAt(deliverable.getWorkOrder().getStartedAt())
                    .completedAt(deliverable.getWorkOrder().getCompletedAt())
                    .createdAt(deliverable.getWorkOrder().getCreatedAt())
                    .build());
        }

        return builder.build();
    }
}
