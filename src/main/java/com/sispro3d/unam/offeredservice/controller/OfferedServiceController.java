package com.sispro3d.unam.offeredservice.controller;

import com.sispro3d.unam.offeredservice.dto.OfferedServiceResponse;
import com.sispro3d.unam.offeredservice.service.OfferedServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class OfferedServiceController {

    @Autowired
    private OfferedServiceService offeredServiceService;

    public void displayService(long id) {
        System.out.println("Displaying offered service with id = " + id);
        Optional<OfferedServiceResponse> serviceDTO = offeredServiceService.findById(id);
        System.out.println("serviceDTO = " + serviceDTO);
    }

    public void displayAllServices() {
        System.out.println("Displaying all offered services:");
        List<OfferedServiceResponse> services = offeredServiceService.findAll();
        services.forEach(System.out::println);
    }
}
