package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.ThreadRequestDTO;
import com.sispro3d.unam.api.dto.ThreadResponseDTO;
import com.sispro3d.unam.api.service.ApiThreadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@Validated
@RequestMapping("/api/v1")
@Slf4j
public class ThreadApiController {

    private final ApiThreadService threadService;

    public ThreadApiController(ApiThreadService threadService) {
        this.threadService = threadService;
    }

    @PostMapping("/work-orders/{orderId}/thread")
    public ResponseEntity<ThreadResponseDTO> create(@PathVariable @Positive Long orderId,
                                                    @Valid @RequestBody ThreadRequestDTO request) {
        log.debug("POST /api/v1/work-orders/{}/thread", orderId);
        ThreadResponseDTO created = threadService.create(orderId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/threads/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/work-orders/{orderId}/thread")
    public ResponseEntity<ThreadResponseDTO> findByWorkOrderId(@PathVariable @Positive Long orderId) {
        log.debug("GET /api/v1/work-orders/{}/thread", orderId);
        return ResponseEntity.ok(threadService.findByWorkOrderId(orderId));
    }

    @GetMapping("/threads/{id}")
    public ResponseEntity<ThreadResponseDTO> findById(@PathVariable @Positive Long id) {
        log.debug("GET /api/v1/threads/{}", id);
        return ResponseEntity.ok(threadService.findById(id));
    }

    @DeleteMapping("/threads/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        log.debug("DELETE /api/v1/threads/{}", id);
        threadService.delete(id);
        return ResponseEntity.noContent().build();
    }
}