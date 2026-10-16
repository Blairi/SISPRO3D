package com.sispro3d.unam.deliverable.service.impl;

import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.core.storage.FileStorageService;
import com.sispro3d.unam.core.storage.UploadValidator;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import com.sispro3d.unam.deliverable.mapper.DeliverableMapper;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.preview.domain.Preview;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
public class DeliverableServiceImpl implements DeliverableService {

    private static final String SUBFOLDER = "deliverables";

    @Autowired
    private DeliverableRepository deliverableRepository;
    @Autowired
    private DeliverableMapper deliverableMapper;
    @Autowired
    private WorkOrderRepository workOrderRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private UploadValidator uploadValidator;
    @Autowired
    private PreviewRepository previewRepository;

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

        String error = uploadValidator.deliverableError(request.getFile());
        if (error != null) {
            log.warn("Archivo de entregable rechazado: orden={}, motivo={}", request.getWorkOrderId(), error);
            throw new BadRequestException(error);
        }

        Deliverable deliverable = deliverableMapper.toEntity(request);
        deliverable.setWorkOrder(order);
        deliverable.setCreatedAt(LocalDateTime.now());
        deliverable.setUrlFile(fileStorageService.store(request.getFile(), SUBFOLDER));
        deliverable.setFileType(fileTypeOf(request));

        Deliverable saved = deliverableRepository.save(deliverable);
        log.info("Entregable creado: id={}, orden={}", saved.getId(), request.getWorkOrderId());
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

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            String error = uploadValidator.deliverableError(request.getFile());
            if (error != null) {
                log.warn("Archivo de entregable rechazado en actualización: entregable={}, motivo={}", id, error);
                throw new BadRequestException(error);
            }
            String previous = existing.getUrlFile();
            existing.setUrlFile(fileStorageService.store(request.getFile(), SUBFOLDER));
            existing.setFileType(fileTypeOf(request));
            fileStorageService.delete(previous);
        }

        Deliverable updated = deliverableRepository.save(existing);
        log.info("Entregable actualizado: id={}", updated.getId());
        return deliverableMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Deliverable existing = deliverableRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Deliverable", id));

        List<Preview> previews = previewRepository.findByDeliverable_Id(id);
        previews.forEach(preview -> fileStorageService.delete(preview.getUrlFile()));
        previewRepository.deleteAll(previews);

        deliverableRepository.delete(existing);
        fileStorageService.delete(existing.getUrlFile());
        log.info("Entregable eliminado: id={}, previsualizaciones={}", id, previews.size());
    }

    @Override
    public boolean existsById(Long id) {
        return deliverableRepository.existsById(id);
    }

    private void requireExpertOwner(WorkOrder order, Long expertId) {
        var expert = accountRepository.findById(expertId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", expertId));

        if (expert.getRole() != Role.EXPERT) {
            log.warn("Intento de gestionar entregables por cuenta no experta: id={}", expertId);
            throw new ForbiddenException("Solo cuentas de tipo EXPERT pueden gestionar entregables");
        }

        var serviceExpert = order.getQuote().getOfferedService().getExpert();
        if (serviceExpert == null || !expertId.equals(serviceExpert.getIdUser())) {
            log.warn("Intento de gestionar entregables por experto no propietario: orden={}, experto={}", order.getId(), expertId);
            throw new ForbiddenException("Solo el experto propietario del servicio puede gestionar la orden");
        }
    }

    private void requireActiveOrder(WorkOrder order) {
        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS
                && order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            log.warn("Intento de gestionar entregables en orden no activa: orden={}, estado={}", order.getId(), order.getStatus());
            throw new BadRequestException("Solo se pueden gestionar entregables en órdenes en estado IN_PROGRESS o IN_REVIEW");
        }
    }

    private String fileTypeOf(DeliverableRequest request) {
        return FileStorageService.extensionOf(request.getFile().getOriginalFilename()).toUpperCase(Locale.ROOT);
    }

    @Override
    public List<DeliverableResponse> findByWorkOrderId(Long workOrderId) {
        return deliverableRepository.findByWorkOrder_Id(workOrderId).stream()
                .map(deliverableMapper::toResponse)
                .toList();
    }
}
