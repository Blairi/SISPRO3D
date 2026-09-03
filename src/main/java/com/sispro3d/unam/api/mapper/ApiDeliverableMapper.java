package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.DeliverableRequestDTO;
import com.sispro3d.unam.api.dto.DeliverableResponseDTO;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import org.springframework.stereotype.Component;

@Component
public class ApiDeliverableMapper {

    public Deliverable toEntity(DeliverableRequestDTO request) {
        if (request == null) {
            return null;
        }
        Deliverable deliverable = new Deliverable();
        deliverable.setName(request.getName());
        deliverable.setUrlFile(request.getUrlFile());
        deliverable.setFileType(request.getFileType());
        return deliverable;
    }

    public DeliverableResponseDTO toResponse(Deliverable entity) {
        if (entity == null) {
            return null;
        }
        return DeliverableResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .urlFile(entity.getUrlFile())
                .fileType(entity.getFileType())
                .createdAt(entity.getCreatedAt())
                .workOrderId(entity.getWorkOrder() != null ? entity.getWorkOrder().getId() : null)
                .build();
    }

    public void updateEntityFromRequest(DeliverableRequestDTO request, Deliverable entity) {
        entity.setName(request.getName());
        entity.setUrlFile(request.getUrlFile());
        entity.setFileType(request.getFileType());
    }
}
