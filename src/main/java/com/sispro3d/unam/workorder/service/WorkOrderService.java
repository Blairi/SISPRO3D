package com.sispro3d.unam.workorder.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;

import java.util.List;
import java.util.Optional;

public interface WorkOrderService extends CrudService<WorkOrderRequest, WorkOrderResponse, Long> {
    WorkOrderResponse start(Long id, Long expertId);
    WorkOrderResponse markInReview(Long id, Long expertId);
    WorkOrderResponse requestChanges(Long id, Long clientId);
    WorkOrderResponse complete(Long id, Long clientId);
    WorkOrderResponse cancel(Long id, Long clientId);
    Optional<WorkOrderResponse> findByQuoteId(Long quoteId);
    List<WorkOrderResponse> findByClientId(Long clientId);
    List<WorkOrderResponse> findByExpertId(Long expertId);
    List<WorkOrderResponse> findByStatus(WorkOrderStatus status);
}
