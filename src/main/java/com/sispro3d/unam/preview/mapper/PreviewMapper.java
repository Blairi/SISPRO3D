package com.sispro3d.unam.preview.mapper;

import com.sispro3d.unam.preview.domain.Preview;
import com.sispro3d.unam.preview.dto.PreviewRequest;
import com.sispro3d.unam.preview.dto.PreviewResponse;
import org.springframework.stereotype.Component;

@Component
public class PreviewMapper {

    /**
     * Maps the flat/scalar fields only. The deliverable relation is NOT set
     * here — the service layer resolves and assigns it after validation.
     */
    public Preview toEntity(PreviewRequest request) {
        if (request == null) {
            return null;
        }
        var preview = new Preview();
        preview.setCaption(request.getCaption());
        preview.setUrlFile(request.getUrlFile());
        return preview;
    }

    public PreviewResponse toResponse(Preview preview) {
        if (preview == null) {
            return null;
        }
        return PreviewResponse.builder()
                .id(preview.getId())
                .caption(preview.getCaption())
                .urlFile(preview.getUrlFile())
                .deliverableId(preview.getDeliverable() != null ? preview.getDeliverable().getId() : null)
                .build();
    }

    public void updateEntityFromRequest(PreviewRequest request, Preview preview) {
        preview.setCaption(request.getCaption());
        preview.setUrlFile(request.getUrlFile());
    }
}
