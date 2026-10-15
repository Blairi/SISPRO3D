package com.sispro3d.unam.workorder.mapper;

import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderMapper {

    /**
     * The request has no scalar fields that map to the entity — status and
     * timestamps are business-controlled. The service layer assigns the
     * quote relation and lifecycle fields after validation.
     */
    public WorkOrder toEntity(WorkOrderRequest request) {
        return new WorkOrder();
    }

    public WorkOrderResponse toResponse(WorkOrder order) {
        if (order == null) {
            return null;
        }
        return WorkOrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .startedAt(order.getStartedAt())
                .completedAt(order.getCompletedAt())
                .createdAt(order.getCreatedAt())
                .quoteId(order.getQuote() != null ? order.getQuote().getId() : null)
                .clientId(order.getQuote() != null && order.getQuote().getClient() != null
                        ? order.getQuote().getClient().getIdUser() : null)
                .offeredServiceId(order.getQuote() != null && order.getQuote().getOfferedService() != null
                        ? order.getQuote().getOfferedService().getId() : null)
                .build();
    }
}
