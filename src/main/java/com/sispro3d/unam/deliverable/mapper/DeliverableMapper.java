package com.sispro3d.unam.deliverable.mapper;

import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;
import org.springframework.stereotype.Component;

@Component
public class DeliverableMapper {

    /**
     * Maps the flat/scalar fields only. The workOrder relation is NOT set
     * here — the service layer resolves and assigns it after validation.
     */
    public Deliverable toEntity(DeliverableRequest request) {
        if (request == null) {
            return null;
        }
        var deliverable = new Deliverable();
        deliverable.setName(request.getName());
        deliverable.setUrlFile(request.getUrlFile());
        deliverable.setFileType(request.getFileType());
        return deliverable;
    }

    public DeliverableResponse toResponse(Deliverable deliverable) {
        if (deliverable == null) {
            return null;
        }
        return DeliverableResponse.builder()
                .id(deliverable.getId())
                .name(deliverable.getName())
                .urlFile(deliverable.getUrlFile())
                .fileType(deliverable.getFileType())
                .createdAt(deliverable.getCreatedAt())
                .workOrderId(deliverable.getWorkOrder() != null ? deliverable.getWorkOrder().getId() : null)
                .build();
    }

    public void updateEntityFromRequest(DeliverableRequest request, Deliverable deliverable) {
        deliverable.setName(request.getName());
        deliverable.setUrlFile(request.getUrlFile());
        deliverable.setFileType(request.getFileType());
    }
}
