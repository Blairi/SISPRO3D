package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.ThreadResponseDTO;
import com.sispro3d.unam.thread.domain.Thread;
import org.springframework.stereotype.Component;

@Component
public class ApiThreadMapper {

    public ThreadResponseDTO toResponse(Thread entity) {
        if (entity == null) {
            return null;
        }
        return ThreadResponseDTO.builder()
                .id(entity.getId())
                .workOrderId(entity.getWorkOrder() != null ? entity.getWorkOrder().getId() : null)
                .build();
    }
}