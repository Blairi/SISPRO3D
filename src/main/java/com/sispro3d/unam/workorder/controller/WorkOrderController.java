package com.sispro3d.unam.workorder.controller;

import com.sispro3d.unam.workorder.dto.WorkOrderResponse;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    public void displayWorkOrder(long id) {
        System.out.println("Displaying work order with id = " + id);
        Optional<WorkOrderResponse> workOrder = workOrderService.findById(id);
        System.out.println("workOrder = " + workOrder);
    }

    public void displayAllWorkOrders() {
        System.out.println("Displaying all work orders:");
        List<WorkOrderResponse> workOrders = workOrderService.findAll();
        workOrders.forEach(System.out::println);
    }
}
