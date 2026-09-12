package com.sispro3d.unam.api.service;

import com.sispro3d.unam.api.dto.PreviewRequestDTO;
import com.sispro3d.unam.api.dto.PreviewResponseDTO;
import com.sispro3d.unam.api.exception.InvalidRequestException;
import com.sispro3d.unam.api.mapper.ApiPreviewMapper;
import com.sispro3d.unam.core.exception.ResourceNotFoundException;
import com.sispro3d.unam.deliverable.repository.DeliverableRepository;
import com.sispro3d.unam.preview.domain.Preview;
import com.sispro3d.unam.preview.repository.PreviewRepository;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.repository.AccountRepository;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiPreviewService {

    private final PreviewRepository previewRepository;
    private final DeliverableRepository deliverableRepository;
    private final AccountRepository accountRepository;
    private final ApiPreviewMapper mapper;

    public ApiPreviewService(PreviewRepository previewRepository,
                             DeliverableRepository deliverableRepository,
                             AccountRepository accountRepository,
                             ApiPreviewMapper mapper) {
        this.previewRepository = previewRepository;
        this.deliverableRepository = deliverableRepository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    public PreviewResponseDTO create(Long deliverableId, PreviewRequestDTO request) {
        var deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Deliverable", deliverableId));

        var expert = accountRepository.findById(request.getExpertId())
                .orElseThrow(() -> ResourceNotFoundException.forId("Account (expert)", request.getExpertId()));
        if (expert.getRole() != Role.EXPERT) {
            throw new InvalidRequestException("Only EXPERT accounts can add previews");
        }

        var order = deliverable.getWorkOrder();
        if (order.getQuote().getOfferedService().getExpert() == null
                || !expert.getIdUser().equals(order.getQuote().getOfferedService().getExpert().getIdUser())) {
            throw new InvalidRequestException("Only the expert owning the order can add previews");
        }

        WorkOrderStatus status = order.getStatus();
        if (status != WorkOrderStatus.IN_PROGRESS && status != WorkOrderStatus.IN_REVIEW) {
            throw new InvalidRequestException("Previews can only be added while the order is IN_PROGRESS or IN_REVIEW");
        }

        Preview preview = new Preview();
        preview.setDeliverable(deliverable);
        preview.setCaption(request.getCaption());
        preview.setUrlFile(request.getUrlFile());

        return mapper.toResponse(previewRepository.save(preview));
    }

    public List<PreviewResponseDTO> findByDeliverableId(Long deliverableId) {
        if (!deliverableRepository.existsById(deliverableId)) {
            throw ResourceNotFoundException.forId("Deliverable", deliverableId);
        }
        return previewRepository.findByDeliverable_Id(deliverableId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PreviewResponseDTO findById(Long id) {
        return previewRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forId("Preview", id));
    }

    public void delete(Long id) {
        if (!previewRepository.existsById(id)) {
            throw ResourceNotFoundException.forId("Preview", id);
        }
        previewRepository.deleteById(id);
    }
}