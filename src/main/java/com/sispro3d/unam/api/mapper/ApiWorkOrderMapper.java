package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.WorkOrderResponseDTO;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import org.springframework.stereotype.Component;

@Component
public class ApiWorkOrderMapper {

    public WorkOrderResponseDTO toResponse(WorkOrder entity) {
        if (entity == null) {
            return null;
        }
        return WorkOrderResponseDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .quoteId(entity.getQuote() != null ? entity.getQuote().getId() : null)
                .clientId(entity.getQuote() != null && entity.getQuote().getClient() != null
                        ? entity.getQuote().getClient().getIdUser() : null)
                .offeredServiceId(entity.getQuote() != null && entity.getQuote().getOfferedService() != null
                        ? entity.getQuote().getOfferedService().getId() : null)
                .build();
    }
}
