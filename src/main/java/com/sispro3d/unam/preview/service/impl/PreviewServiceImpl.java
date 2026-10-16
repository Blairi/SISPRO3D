package com.sispro3d.unam.preview.service.impl;

import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.core.storage.FileStorageService;
import com.sispro3d.unam.core.storage.UploadValidator;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.preview.domain.Preview;
import com.sispro3d.unam.preview.dto.PreviewRequest;
import com.sispro3d.unam.preview.dto.PreviewResponse;
import com.sispro3d.unam.preview.mapper.PreviewMapper;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.preview.service.PreviewService;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PreviewServiceImpl implements PreviewService {

    private static final String SUBFOLDER = "previews";

    private final PreviewRepository previewRepository;
    private final PreviewMapper previewMapper;
    private final DeliverableRepository deliverableRepository;
    private final AccountRepository accountRepository;
    private final FileStorageService fileStorageService;
    private final UploadValidator uploadValidator;

    public PreviewServiceImpl(PreviewRepository previewRepository,
                              PreviewMapper previewMapper,
                              DeliverableRepository deliverableRepository,
                              AccountRepository accountRepository,
                              FileStorageService fileStorageService,
                              UploadValidator uploadValidator) {
        this.previewRepository = previewRepository;
        this.previewMapper = previewMapper;
        this.deliverableRepository = deliverableRepository;
        this.accountRepository = accountRepository;
        this.fileStorageService = fileStorageService;
        this.uploadValidator = uploadValidator;
    }

    @Override
    public List<PreviewResponse> findAll() {
        return previewRepository.findAll().stream()
                .map(previewMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<PreviewResponse> findById(Long id) {
        return previewRepository.findById(id)
                .map(previewMapper::toResponse);
    }

    @Override
    public PreviewResponse create(PreviewRequest request) {
        Deliverable deliverable = deliverableRepository.findById(request.getDeliverableId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Deliverable", request.getDeliverableId()));

        var order = deliverable.getWorkOrder();
        requireExpertOwner(order, request.getExpertId());
        requireActiveOrder(order);

        String error = uploadValidator.previewError(request.getFile());
        if (error != null) {
            log.warn("Archivo de previsualización rechazado: entregable={}, motivo={}", request.getDeliverableId(), error);
            throw new BadRequestException(error);
        }

        Preview preview = previewMapper.toEntity(request);
        preview.setDeliverable(deliverable);
        preview.setCreatedAt(LocalDateTime.now());
        preview.setUrlFile(fileStorageService.store(request.getFile(), SUBFOLDER));

        Preview saved = previewRepository.save(preview);
        log.info("Previsualización creada: id={}, entregable={}", saved.getId(), request.getDeliverableId());
        return previewMapper.toResponse(saved);
    }

    @Override
    public PreviewResponse update(Long id, PreviewRequest request) {
        Preview existing = previewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Preview", id));

        if (!deliverableRepository.existsById(request.getDeliverableId())) {
            throw ResourceNotFoundException.forId("Deliverable", request.getDeliverableId());
        }

        previewMapper.updateEntityFromRequest(request, existing);
        existing.setDeliverable(deliverableRepository.getReferenceById(request.getDeliverableId()));

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            String error = uploadValidator.previewError(request.getFile());
            if (error != null) {
                log.warn("Archivo de previsualización rechazado en actualización: previsualización={}, motivo={}", id, error);
                throw new BadRequestException(error);
            }
            String previous = existing.getUrlFile();
            existing.setUrlFile(fileStorageService.store(request.getFile(), SUBFOLDER));
            fileStorageService.delete(previous);
        }

        Preview updated = previewRepository.save(existing);
        log.info("Previsualización actualizada: id={}", updated.getId());
        return previewMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Preview existing = previewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Preview", id));
        previewRepository.deleteById(id);
        fileStorageService.delete(existing.getUrlFile());
        log.info("Previsualización eliminada: id={}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return previewRepository.existsById(id);
    }

    private void requireExpertOwner(WorkOrder order, Long expertId) {
        var expert = accountRepository.findById(expertId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", expertId));

        if (expert.getRole() != Role.EXPERT) {
            log.warn("Intento de gestionar previsualizaciones por cuenta no experta: id={}", expertId);
            throw new ForbiddenException("Solo cuentas de tipo EXPERT pueden gestionar previsualizaciones");
        }

        var serviceExpert = order.getQuote().getOfferedService().getExpert();
        if (serviceExpert == null || !expertId.equals(serviceExpert.getIdUser())) {
            log.warn("Intento de gestionar previsualizaciones por experto no propietario: orden={}, experto={}", order.getId(), expertId);
            throw new ForbiddenException("Solo el experto propietario del servicio puede gestionar la orden");
        }
    }

    private void requireActiveOrder(WorkOrder order) {
        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS
                && order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            log.warn("Intento de gestionar previsualizaciones en orden no activa: orden={}, estado={}", order.getId(), order.getStatus());
            throw new BadRequestException("Solo se pueden gestionar previsualizaciones en órdenes en estado IN_PROGRESS o IN_REVIEW");
        }
    }

    @Override
    public List<PreviewResponse> findByDeliverableId(Long deliverableId) {
        return previewRepository.findByDeliverable_IdOrderByCreatedAtAsc(deliverableId).stream()
                .map(previewMapper::toResponse)
                .toList();
    }
}
