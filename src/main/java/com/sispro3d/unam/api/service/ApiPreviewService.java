package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.PreviewRequestDTO;
import com.sispro3d.unam.api.dto.PreviewResponseDTO;
import com.sispro3d.unam.api.exception.InvalidRequestException;
import com.sispro3d.unam.api.mapper.ApiPreviewMapper;
import com.sispro3d.unam.core.exception.BadRequestException;
import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.security.ApiCurrentUser;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.preview.domain.Preview;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiPreviewService {

    private final PreviewRepository previewRepository;
    private final DeliverableRepository deliverableRepository;
    private final AccountRepository accountRepository;
    private final ApiPreviewMapper mapper;
    private final ApiOrderAccess access;

    public ApiPreviewService(PreviewRepository previewRepository,
                             DeliverableRepository deliverableRepository,
                             AccountRepository accountRepository,
                             ApiPreviewMapper mapper,
                             ApiOrderAccess access) {
        this.previewRepository = previewRepository;
        this.deliverableRepository = deliverableRepository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
        this.access = access;
    }

    public PreviewResponseDTO create(Long deliverableId, PreviewRequestDTO request) {
        var deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Deliverable", deliverableId));

        Long expertId = ApiCurrentUser.requireAccountId();
        var expert = accountRepository.findById(expertId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", expertId));
        if (expert.getRole() != Role.EXPERT) {
            throw new ForbiddenException("Only EXPERT accounts can add previews");
        }

        var order = deliverable.getWorkOrder();
        if (order.getQuote().getOfferedService().getExpert() == null
                || !expert.getIdUser().equals(order.getQuote().getOfferedService().getExpert().getIdUser())) {
            throw new ForbiddenException("Only the expert owning the order can add previews");
        }

        WorkOrderStatus status = order.getStatus();
        if (status != WorkOrderStatus.IN_PROGRESS && status != WorkOrderStatus.IN_REVIEW) {
            throw new InvalidRequestException("Previews can only be added while the order is IN_PROGRESS or IN_REVIEW");
        }

        Preview preview = new Preview();
        preview.setDeliverable(deliverable);
        preview.setCaption(request.getCaption());
        preview.setUrlFile(request.getUrlFile());
        preview.setCreatedAt(LocalDateTime.now());

        return mapper.toResponse(previewRepository.save(preview));
    }

    public List<PreviewResponseDTO> findByDeliverableId(Long deliverableId) {
        var deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Deliverable", deliverableId));

        access.requireParticipant(deliverable.getWorkOrder());

        return previewRepository.findByDeliverable_Id(deliverableId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PreviewResponseDTO findById(Long id) {
        var preview = findPreview(id);
        access.requireParticipant(preview.getDeliverable().getWorkOrder());
        return mapper.toResponse(preview);
    }

    /** Actualizar un preview lo restringe al experto y a las fases activas. */
    public PreviewResponseDTO update(Long id, PreviewRequestDTO request) {
        var preview = findPreview(id);
        var order = preview.getDeliverable().getWorkOrder();

        access.requireExpertOwner(order);

        if (order.getStatus() != WorkOrderStatus.IN_PROGRESS
                && order.getStatus() != WorkOrderStatus.IN_REVIEW) {
            throw new BadRequestException(
                    "Previews can only be updated while the order is IN_PROGRESS or IN_REVIEW");
        }

        preview.setCaption(request.getCaption());
        preview.setUrlFile(request.getUrlFile());
        return mapper.toResponse(previewRepository.save(preview));
    }

    public void delete(Long id) {
        var preview = findPreview(id);
        access.requireParticipant(preview.getDeliverable().getWorkOrder());
        previewRepository.delete(preview);
    }

    private Preview findPreview(Long id) {
        return previewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId("Preview", id));
    }
}