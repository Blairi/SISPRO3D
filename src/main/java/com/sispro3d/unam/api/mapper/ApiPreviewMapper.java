package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.PreviewResponseDTO;
import com.sispro3d.unam.preview.domain.Preview;
import org.springframework.stereotype.Component;

@Component
public class ApiPreviewMapper {

    public PreviewResponseDTO toResponse(Preview entity) {
        if (entity == null) {
            return null;
        }
        return PreviewResponseDTO.builder()
                .id(entity.getId())
                .deliverableId(entity.getDeliverable() != null ? entity.getDeliverable().getId() : null)
                .caption(entity.getCaption())
                .urlFile(entity.getUrlFile())
                .build();
    }
}