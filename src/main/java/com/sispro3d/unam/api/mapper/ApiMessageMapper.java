package com.sispro3d.unam.api.mapper;

import com.sispro3d.unam.api.dto.MessageResponseDTO;
import com.sispro3d.unam.message.domain.Message;
import org.springframework.stereotype.Component;

@Component
public class ApiMessageMapper {

    public MessageResponseDTO toResponse(Message entity) {
        if (entity == null) {
            return null;
        }
        return MessageResponseDTO.builder()
                .id(entity.getId())
                .threadId(entity.getThread() != null ? entity.getThread().getId() : null)
                .userId(entity.getAccount() != null ? entity.getAccount().getIdUser() : null)
                .content(entity.getContent())
                .timestamp(entity.getTimestamp())
                .build();
    }
}