package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.ServiceJdbcDAO;
import mx.unam.dgtic.dto.ServiceDTO;
import mx.unam.dgtic.service.ServiceService;
import mx.unam.dgtic.service.impl.ServiceServiceImpl;

import java.util.Optional;

public class ServiceController {
    private ServiceService serviceService;

    public ServiceController() {
        this.serviceService = new ServiceServiceImpl(new ServiceJdbcDAO());
    }

    public void displayService(int id) {
        System.out.println("Displaying service with id = " + id);
        Optional<ServiceDTO> serviceDTO = serviceService.findById(id);
        System.out.println("serviceDTO = " + serviceDTO);
    }

    public void displayAllServices() {
        System.out.println("Displaying all services:");
        serviceService.findAll().forEach(System.out::println);
    }
}
