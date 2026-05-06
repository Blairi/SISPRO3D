package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.DeliverableJdbcDAO;
import mx.unam.dgtic.dto.DeliverableDTO;
import mx.unam.dgtic.service.DeliverableService;
import mx.unam.dgtic.service.impl.DeliverableServiceImpl;

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
