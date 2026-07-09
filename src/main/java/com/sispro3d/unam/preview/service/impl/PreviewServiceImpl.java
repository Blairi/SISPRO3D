package com.sispro3d.unam.preview.service.impl;

import com.sispro3d.unam.core.dao.GenericDAO;
import com.sispro3d.unam.core.dto.DeliverableRef;
import com.sispro3d.unam.deliverable.domain.Deliverable;
import com.sispro3d.unam.preview.domain.Preview;
import com.sispro3d.unam.preview.dto.PreviewDTO;
import com.sispro3d.unam.preview.service.PreviewService;

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
            DeliverableRef delRef = DeliverableRef.builder()
                    .id(preview.getDeliverable().getId())
                    .name(preview.getDeliverable().getName())
                    .urlFile(preview.getDeliverable().getUrlFile())
                    .fileType(preview.getDeliverable().getFileType())
                    .createdAt(preview.getDeliverable().getCreatedAt())
                    .build();
            dto.setDeliverable(delRef);
        }

        return dto;
    }
}
