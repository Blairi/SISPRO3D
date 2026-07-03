package com.sispro3d.unam.controller;

import com.sispro3d.unam.dao.PreviewJdbcDAO;
import com.sispro3d.unam.dto.PreviewDTO;
import com.sispro3d.unam.service.PreviewService;
import com.sispro3d.unam.service.impl.PreviewServiceImpl;

import java.util.Optional;

public class PreviewController {
    private PreviewService previewService;

    public PreviewController() {
        this.previewService = new PreviewServiceImpl(new PreviewJdbcDAO());
    }

    public void displayPreview(int id) {
        System.out.println("Displaying preview with id = " + id);
        Optional<PreviewDTO> previewDTO = previewService.findById(id);
        System.out.println("previewDTO = " + previewDTO);
    }

    public void displayAllPreviews() {
        System.out.println("Displaying all previews:");
        previewService.findAll().forEach(System.out::println);
    }
}
