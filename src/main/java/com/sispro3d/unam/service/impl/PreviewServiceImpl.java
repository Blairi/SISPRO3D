package com.sispro3d.unam.service.impl;

import com.sispro3d.unam.dao.GenericDAO;
import com.sispro3d.unam.domain.Preview;
import com.sispro3d.unam.domain.Deliverable;
import com.sispro3d.unam.dto.PreviewDTO;
import com.sispro3d.unam.dto.DeliverableDTO;
import com.sispro3d.unam.service.PreviewService;

import java.util.List;
import java.util.Optional;

public class PreviewServiceImpl implements PreviewService {

    private final GenericDAO<Preview> previewDAO;

    public PreviewServiceImpl(GenericDAO<Preview> previewDAO) {
        this.previewDAO = previewDAO;
    }

    @Override
    public List<PreviewDTO> findAll() {
        return previewDAO.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public Optional<PreviewDTO> findById(int id) {
        return previewDAO.findById(id)
                .map(this::toResponseDTO);
    }

    @Override
    public PreviewDTO create(PreviewDTO dto) {
        Preview preview = toEntity(dto);
        int generatedId = previewDAO.insert(preview);
        preview.setId(generatedId);
        return toResponseDTO(preview);
    }

    @Override
    public PreviewDTO update(int id, PreviewDTO dto) {
        previewDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Vista previa no encontrada con id: " + id));

        Preview preview = toEntity(dto);
        preview.setId(id);
        previewDAO.update(preview);
        return toResponseDTO(preview);
    }

    @Override
    public void delete(int id) {
        previewDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Vista previa no encontrada con id: " + id));
        previewDAO.delete(id);
    }

    private Preview toEntity(PreviewDTO dto) {
        Preview preview = new Preview();
        preview.setCaption(dto.getCaption());
        preview.setUrlFile(dto.getUrlFile());

        if (dto.getDeliverable() != null) {
            Deliverable deliverable = new Deliverable(dto.getDeliverable().getId());
            preview.setDeliverable(deliverable);
        }

        return preview;
    }

    private PreviewDTO toResponseDTO(Preview preview) {
        PreviewDTO dto = new PreviewDTO();
        dto.setId(preview.getId());
        dto.setCaption(preview.getCaption());
        dto.setUrlFile(preview.getUrlFile());

        if (preview.getDeliverable() != null) {
            DeliverableDTO delDTO = new DeliverableDTO();
            delDTO.setId(preview.getDeliverable().getId());
            delDTO.setName(preview.getDeliverable().getName());
            delDTO.setUrlFile(preview.getDeliverable().getUrlFile());
            delDTO.setFileType(preview.getDeliverable().getFileType());
            delDTO.setCreatedAt(preview.getDeliverable().getCreatedAt());
            dto.setDeliverable(delDTO);
        }

        return dto;
    }
}
