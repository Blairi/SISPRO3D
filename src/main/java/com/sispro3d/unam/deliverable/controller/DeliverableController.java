package com.sispro3d.unam.deliverable.controller;

import com.sispro3d.unam.deliverable.dao.DeliverableJdbcDAO;
import com.sispro3d.unam.deliverable.dto.DeliverableDTO;
import com.sispro3d.unam.deliverable.service.DeliverableService;
import com.sispro3d.unam.deliverable.service.impl.DeliverableServiceImpl;

import java.util.Optional;

public class DeliverableController {
    private DeliverableService deliverableService;

    public DeliverableController() {
        this.deliverableService = new DeliverableServiceImpl(new DeliverableJdbcDAO());
    }

    public void displayDeliverable(int id) {
        System.out.println("Displaying deliverable with id = " + id);
        Optional<DeliverableDTO> deliverableDTO = deliverableService.findById(id);
        System.out.println("deliverableDTO = " + deliverableDTO);
    }

    public void displayAllDeliverables() {
        System.out.println("Displaying all deliverables:");
        deliverableService.findAll().forEach(System.out::println);
    }
}
