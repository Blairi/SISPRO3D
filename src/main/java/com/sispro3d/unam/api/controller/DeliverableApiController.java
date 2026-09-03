package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.DeliverableRequestDTO;
import com.sispro3d.unam.api.dto.DeliverableResponseDTO;
import com.sispro3d.unam.api.service.ApiDeliverableService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DeliverableApiController {

    private final ApiDeliverableService deliverableService;

    public DeliverableApiController(ApiDeliverableService deliverableService) {
        this.deliverableService = deliverableService;
    }

    @GetMapping("/work-orders/{orderId}/deliverables")
    public ResponseEntity<List<DeliverableResponseDTO>> findByWorkOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliverableService.findByWorkOrderId(orderId));
    }

    @PostMapping("/work-orders/{orderId}/deliverables")
    public ResponseEntity<DeliverableResponseDTO> create(
            @PathVariable Long orderId,
            @Valid @RequestBody DeliverableRequestDTO request) {
        DeliverableResponseDTO created = deliverableService.create(orderId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/../{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/deliverables/{id}")
    public ResponseEntity<DeliverableResponseDTO> update(@PathVariable Long id,
                                                         @Valid @RequestBody DeliverableRequestDTO request) {
        return ResponseEntity.ok(deliverableService.update(id, request));
    }

    @DeleteMapping("/deliverables/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deliverableService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
