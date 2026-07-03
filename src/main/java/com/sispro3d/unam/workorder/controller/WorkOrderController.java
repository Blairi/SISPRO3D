package com.sispro3d.unam.workorder.controller;

import com.sispro3d.unam.workorder.dao.WorkOrderJdbcDAO;
import com.sispro3d.unam.workorder.dto.WorkOrderDTO;
import com.sispro3d.unam.workorder.service.WorkOrderService;
import com.sispro3d.unam.workorder.service.impl.WorkOrderServiceImpl;

import java.util.Optional;

public class WorkOrderController {
    private WorkOrderService workOrderService;

    public WorkOrderController() {
        this.workOrderService = new WorkOrderServiceImpl(new WorkOrderJdbcDAO());
    }

    public void displayWorkOrder(int id) {
        System.out.println("Displaying work order with id = " + id);
        Optional<WorkOrderDTO> workOrderDTO = workOrderService.findById(id);
        System.out.println("workOrderDTO = " + workOrderDTO);
    }

    public void displayAllWorkOrders() {
        System.out.println("Displaying all work orders:");
        workOrderService.findAll().forEach(System.out::println);
    }
}
