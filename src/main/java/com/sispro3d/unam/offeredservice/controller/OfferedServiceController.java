package com.sispro3d.unam.offeredservice.controller;

import com.sispro3d.unam.offeredservice.dao.OfferedServiceJdbcDAO;
import com.sispro3d.unam.offeredservice.dto.OfferedServiceDTO;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import com.sispro3d.unam.offeredservice.service.impl.OfferedServiceServiceImpl;

import java.util.Optional;

public class OfferedServiceController {
    private OfferedServiceService offeredServiceService;

    public OfferedServiceController() {
        this.offeredServiceService = new OfferedServiceServiceImpl(new OfferedServiceJdbcDAO());
    }

    public void displayService(int id) {
        System.out.println("Displaying offered service with id = " + id);
        Optional<OfferedServiceDTO> serviceDTO = offeredServiceService.findById(id);
        System.out.println("serviceDTO = " + serviceDTO);
    }

    public void displayAllServices() {
        System.out.println("Displaying all offered services:");
        offeredServiceService.findAll().forEach(System.out::println);
    }
}
