package com.sispro3d.unam.deliverable.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.deliverable.dto.DeliverableRequest;
import com.sispro3d.unam.deliverable.dto.DeliverableResponse;

import java.util.List;

public interface DeliverableService extends CrudService<DeliverableRequest, DeliverableResponse, Long> {
    List<DeliverableResponse> findByWorkOrderId(Long workOrderId);
}
