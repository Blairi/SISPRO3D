package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.MessageRequestDTO;
import com.sispro3d.unam.api.dto.MessageResponseDTO;
import com.sispro3d.unam.api.service.ApiMessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1")
public class MessageApiController {

    private final ApiMessageService messageService;

    public MessageApiController(ApiMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/threads/{threadId}/messages")
    public ResponseEntity<MessageResponseDTO> post(@PathVariable @Positive Long threadId,
                                                   @Valid @RequestBody MessageRequestDTO request) {
        MessageResponseDTO created = messageService.post(threadId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<List<MessageResponseDTO>> findByThreadId(@PathVariable @Positive Long threadId) {
        return ResponseEntity.ok(messageService.findByThreadId(threadId));
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<MessageResponseDTO> findById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(messageService.findById(id));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}