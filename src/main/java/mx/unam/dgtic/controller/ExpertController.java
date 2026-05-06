package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.ExpertJdbcDAO;
import mx.unam.dgtic.dto.ExpertDTO;
import mx.unam.dgtic.service.ExpertService;
import mx.unam.dgtic.service.impl.ExpertServiceImpl;

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
