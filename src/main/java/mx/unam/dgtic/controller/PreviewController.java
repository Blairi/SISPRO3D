package mx.unam.dgtic.controller;

import mx.unam.dgtic.dao.PreviewJdbcDAO;
import mx.unam.dgtic.dto.PreviewDTO;
import mx.unam.dgtic.service.PreviewService;
import mx.unam.dgtic.service.impl.PreviewServiceImpl;

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
