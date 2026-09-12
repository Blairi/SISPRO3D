package com.sispro3d.unam.preview.service.impl;

import com.sispro3d.unam.core.exception.ResourceNotFoundException;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PreviewServiceImpl implements PreviewService {

    private final PreviewRepository previewRepository;
    private final PreviewMapper previewMapper;
    private final DeliverableRepository deliverableRepository;
    private final AccountRepository accountRepository;

    public PreviewServiceImpl(PreviewRepository previewRepository,
                              PreviewMapper previewMapper,
                              DeliverableRepository deliverableRepository,
                              AccountRepository accountRepository) {
        this.previewRepository = previewRepository;
        this.previewMapper = previewMapper;
        this.deliverableRepository = deliverableRepository;
        this.accountRepository = accountRepository;
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

        Preview preview = previewMapper.toEntity(request);
        preview.setDeliverable(deliverable);

        Preview saved = previewRepository.save(preview);
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
        Preview updated = previewRepository.save(existing);
        return previewMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!previewRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Preview", id);
        }
        previewRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return previewRepository.existsById(id);
    }

    private void requireExpertOwner(WorkOrder order, Long expertId) {
        var expert = accountRepository.findById(expertId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", expertId));

        if (expert.getRole() != Role.EXPERT) {
            throw new IllegalStateException("Solo cuentas de tipo EXPERT pueden gestionar previsualizaciones");
        }

        var serviceExpert = order.getQuote().getOfferedService().getExpert();
        if (serviceExpert == null || !expertId.equals(serviceExpert.getIdUser())) {
            throw new IllegalStateException("Solo el experto propietario del servicio puede gestionar la orden");
        }
    }

    private void requireActiveOrder(WorkOrder order) {
        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS
                && order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            throw new IllegalStateException("Solo se pueden gestionar previsualizaciones en órdenes en estado IN_PROGRESS o IN_REVIEW");
        }
    }

    @Override
    public List<PreviewResponse> findByDeliverableId(Long deliverableId) {
        return previewRepository.findByDeliverable_Id(deliverableId).stream()
                .map(previewMapper::toResponse)
                .toList();
    }
}
