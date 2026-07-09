package com.sispro3d.unam.user.controller;

import com.sispro3d.unam.user.dto.ExpertResponse;
import com.sispro3d.unam.user.service.ExpertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class ExpertController {

    @Autowired
    private ExpertService expertService;

    public void displayExpert(long id) {
        System.out.println("Displaying expert with id = " + id);
        Optional<ExpertResponse> expertDTO = expertService.findById(id);
        System.out.println("expertDTO = " + expertDTO);
    }

    public void displayAllExperts() {
        System.out.println("Displaying all experts:");
        List<ExpertResponse> experts = expertService.findAll();
        experts.forEach(System.out::println);
    }
}
