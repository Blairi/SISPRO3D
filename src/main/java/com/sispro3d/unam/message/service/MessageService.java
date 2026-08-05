package com.sispro3d.unam.message.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.message.dto.MessageRequest;
import com.sispro3d.unam.message.dto.MessageResponse;

import java.util.List;

public interface MessageService extends CrudService<MessageRequest, MessageResponse, Long> {
    List<MessageResponse> findByThreadId(Long threadId);
}
