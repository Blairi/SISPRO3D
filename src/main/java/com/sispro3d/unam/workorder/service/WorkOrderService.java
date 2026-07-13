package com.sispro3d.unam.workorder.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.workorder.dto.WorkOrderRequest;
import com.sispro3d.unam.workorder.dto.WorkOrderResponse;

public interface WorkOrderService extends CrudService<WorkOrderRequest, WorkOrderResponse, Long> {
}
