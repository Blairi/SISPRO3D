package com.sispro3d.unam.controller;

import com.sispro3d.unam.dao.ThreadJdbcDAO;
import com.sispro3d.unam.dto.ThreadDTO;
import com.sispro3d.unam.service.ThreadService;
import com.sispro3d.unam.service.impl.ThreadServiceImpl;

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
