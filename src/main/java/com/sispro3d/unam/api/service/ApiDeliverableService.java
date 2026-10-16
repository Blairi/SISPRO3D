package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.DeliverableRequestDTO;
import com.sispro3d.unam.api.dto.DeliverableResponseDTO;
import com.sispro3d.unam.api.mapper.ApiDeliverableMapper;
import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.core.storage.FileStorageService;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ApiDeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PreviewRepository previewRepository;
    private final ApiDeliverableMapper mapper;
    private final ApiOrderAccess access;
    private final FileStorageService fileStorageService;

    public ApiDeliverableService(DeliverableRepository deliverableRepository,
                                 WorkOrderRepository workOrderRepository,
                                 PreviewRepository previewRepository,
                                 ApiDeliverableMapper mapper,
                                 ApiOrderAccess access,
                                 FileStorageService fileStorageService) {
        this.deliverableRepository = deliverableRepository;
        this.workOrderRepository = workOrderRepository;
        this.previewRepository = previewRepository;
        this.mapper = mapper;
        this.access = access;
        this.fileStorageService = fileStorageService;
    }

    public List<DeliverableResponseDTO> findByWorkOrderId(Long workOrderId) {
        var order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", workOrderId));

        access.requireParticipant(order);

        return deliverableRepository.findByWorkOrder_Id(workOrderId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public DeliverableResponseDTO findById(Long id) {
        var deliverable = findDeliverable(id);
        access.requireParticipant(deliverable.getWorkOrder());
        return mapper.toResponse(deliverable);
    }

    public DeliverableResponseDTO create(Long workOrderId, DeliverableRequestDTO request) {
        var order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> ResourceNotFoundException.forId("WorkOrder", workOrderId));

        access.requireExpertOwner(order);

        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS
                && order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            log.warn("Rechazado: entregable en orden no activa (API): orden={}, estado={}", workOrderId, order.getStatus());
            throw new BadRequestException(
                    "Deliverables can only be registered while the order is IN_PROGRESS or IN_REVIEW");
        }

        var deliverable = mapper.toEntity(request);
        deliverable.setWorkOrder(order);
        deliverable.setCreatedAt(LocalDateTime.now());

        var saved = deliverableRepository.save(deliverable);
        log.info("Entregable creado (API): id={}, orden={}", saved.getId(), workOrderId);
        return mapper.toResponse(saved);
    }

    public DeliverableResponseDTO update(Long id, DeliverableRequestDTO request) {
        var existing = findDeliverable(id);

        access.requireExpertOwner(existing.getWorkOrder());

        mapper.updateEntityFromRequest(request, existing);
        var updated = deliverableRepository.save(existing);
        log.info("Entregable actualizado (API): id={}", updated.getId());
        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        var existing = findDeliverable(id);

        access.requireParticipant(existing.getWorkOrder());

        var previews = previewRepository.findByDeliverable_Id(id);
        previews.forEach(preview -> fileStorageService.delete(preview.getUrlFile()));
        previewRepository.deleteAll(previews);
        deliverableRepository.delete(existing);
        fileStorageService.delete(existing.getUrlFile());
        log.info("Entregable eliminado (API): id={}", id);
    }

    private Deliverable findDeliverable(Long id) {
        return deliverableRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Deliverable", id));
    }
}
