package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.dao.ExpertJdbcDAO;
import com.sispro3d.unam.user.dto.ExpertDTO;
import com.sispro3d.unam.user.service.ExpertService;
import com.sispro3d.unam.user.service.impl.ExpertServiceImpl;

import java.util.Optional;

public class ExpertController {
    private ExpertService expertService;

    public ExpertController() {
        this.expertService = new ExpertServiceImpl(new ExpertJdbcDAO());
    }

    public void displayExpert(int id) {
        System.out.println("Displaying expert with id = " + id);
        Optional<ExpertDTO> expertDTO = expertService.findById(id);
        System.out.println("expertDTO = " + expertDTO);
    }

    public void displayAllExperts() {
        System.out.println("Displaying all experts:");
        expertService.findAll().forEach(System.out::println);
    }
}
