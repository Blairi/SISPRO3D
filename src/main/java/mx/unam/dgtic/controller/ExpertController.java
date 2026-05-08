package mx.unam.dgtic.controller;

import mx.unam.dgtic.dto.ExpertDTO;
import mx.unam.dgtic.service.ExpertService;
import mx.unam.dgtic.service.impl.ExpertServiceImpl;

import java.util.List;

public class ExpertController {

    private final ExpertService expertService;

    public ExpertController() {
        this.expertService = new ExpertServiceImpl();
    }

    public void displayExpert(int id) {
        System.out.println(expertService.findById(id));
    }

    public void displayAllExperts() {
        List<ExpertDTO> expertDTOS = expertService.findAll();
        expertDTOS.forEach(System.out::println);
    }
}
