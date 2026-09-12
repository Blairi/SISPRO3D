package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.PreviewRequestDTO;
import com.sispro3d.unam.api.dto.PreviewResponseDTO;
import com.sispro3d.unam.api.service.ApiPreviewService;
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
public class PreviewApiController {

    private final ApiPreviewService previewService;

    public PreviewApiController(ApiPreviewService previewService) {
        this.previewService = previewService;
    }

    @PostMapping("/deliverables/{deliverableId}/previews")
    public ResponseEntity<PreviewResponseDTO> create(@PathVariable @Positive Long deliverableId,
                                                     @Valid @RequestBody PreviewRequestDTO request) {
        PreviewResponseDTO created = previewService.create(deliverableId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/deliverables/{deliverableId}/previews")
    public ResponseEntity<List<PreviewResponseDTO>> findByDeliverableId(@PathVariable @Positive Long deliverableId) {
        return ResponseEntity.ok(previewService.findByDeliverableId(deliverableId));
    }

    @GetMapping("/previews/{id}")
    public ResponseEntity<PreviewResponseDTO> findById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(previewService.findById(id));
    }

    @DeleteMapping("/previews/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        previewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}