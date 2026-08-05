package com.sispro3d.unam.message.mapper;

import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.message.dto.MessageRequest;
import com.sispro3d.unam.message.dto.MessageResponse;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    /**
     * Maps the flat/scalar fields only. Relations (thread, account) are NOT
     * set here — the service layer resolves and assigns them after validation.
     */
    public Message toEntity(MessageRequest request) {
        if (request == null) {
            return null;
        }
        var message = new Message();
        message.setContent(request.getContent());
        return message;
    }

    public MessageResponse toResponse(Message message) {
        if (message == null) {
            return null;
        }
        return MessageResponse.builder()
                .id(message.getId())
                .threadId(message.getThread() != null ? message.getThread().getId() : null)
                .userId(message.getAccount() != null ? message.getAccount().getIdUser() : null)
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }

    public void updateEntityFromRequest(MessageRequest request, Message message) {
        message.setContent(request.getContent());
    }
}
