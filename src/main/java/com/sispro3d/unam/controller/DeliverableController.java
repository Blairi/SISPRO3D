package com.sispro3d.unam.controller;

import com.sispro3d.unam.dao.DeliverableJdbcDAO;
import com.sispro3d.unam.dto.DeliverableDTO;
import com.sispro3d.unam.service.DeliverableService;
import com.sispro3d.unam.service.impl.DeliverableServiceImpl;

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
