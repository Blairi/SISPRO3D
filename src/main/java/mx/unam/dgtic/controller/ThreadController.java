package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.ThreadJdbcDAO;
import mx.unam.dgtic.dto.ThreadDTO;
import mx.unam.dgtic.service.ThreadService;
import mx.unam.dgtic.service.impl.ThreadServiceImpl;

import java.util.Optional;

public class ThreadController {
    private ThreadService threadService;

    public ThreadController() {
        this.threadService = new ThreadServiceImpl(new ThreadJdbcDAO());
    }

    public void displayThread(int id) {
        System.out.println("Displaying thread with id = " + id);
        Optional<ThreadDTO> threadDTO = threadService.findById(id);
        System.out.println("threadDTO = " + threadDTO);
    }

    public void displayAllThreads() {
        System.out.println("Displaying all threads:");
        threadService.findAll().forEach(System.out::println);
    }
}
