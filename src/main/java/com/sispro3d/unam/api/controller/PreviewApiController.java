package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.PreviewRequestDTO;
import com.sispro3d.unam.api.dto.PreviewResponseDTO;
import com.sispro3d.unam.api.service.ApiPreviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
@RequestMapping("/api/v1")
@Slf4j
public class PreviewApiController {

    private final ApiPreviewService previewService;

    public PreviewApiController(ApiPreviewService previewService) {
        this.previewService = previewService;
    }

    @PostMapping("/deliverables/{deliverableId}/previews")
    public ResponseEntity<PreviewResponseDTO> create(@PathVariable @Positive Long deliverableId,
                                                     @Valid @RequestBody PreviewRequestDTO request) {
        log.debug("POST /api/v1/deliverables/{}/previews", deliverableId);
        PreviewResponseDTO created = previewService.create(deliverableId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/deliverables/{deliverableId}/previews")
    public ResponseEntity<List<PreviewResponseDTO>> findByDeliverableId(@PathVariable @Positive Long deliverableId) {
        log.debug("GET /api/v1/deliverables/{}/previews", deliverableId);
        return ResponseEntity.ok(previewService.findByDeliverableId(deliverableId));
    }

    @PutMapping("/previews/{id}")
    public ResponseEntity<PreviewResponseDTO> update(@PathVariable @Positive Long id,
                                                     @Valid @RequestBody PreviewRequestDTO request) {
        log.debug("PUT /api/v1/previews/{}", id);
        return ResponseEntity.ok(previewService.update(id, request));
    }

    @GetMapping("/previews/{id}")
    public ResponseEntity<PreviewResponseDTO> findById(@PathVariable @Positive Long id) {
        log.debug("GET /api/v1/previews/{}", id);
        return ResponseEntity.ok(previewService.findById(id));
    }

    @DeleteMapping("/previews/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        log.debug("DELETE /api/v1/previews/{}", id);
        previewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}