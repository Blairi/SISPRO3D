package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.WorkOrderJdbcDAO;
import mx.unam.dgtic.dto.WorkOrderDTO;
import mx.unam.dgtic.service.WorkOrderService;
import mx.unam.dgtic.service.impl.WorkOrderServiceImpl;

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
