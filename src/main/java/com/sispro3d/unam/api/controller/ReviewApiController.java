package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.ReviewRequestDTO;
import com.sispro3d.unam.api.dto.ReviewResponseDTO;
import com.sispro3d.unam.api.service.ApiReviewService;
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
public class ReviewApiController {

    private final ApiReviewService reviewService;

    public ReviewApiController(ApiReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/services/{serviceId}/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> findByServiceId(@PathVariable Long serviceId) {
        return ResponseEntity.ok(reviewService.findByServiceId(serviceId));
    }

    @PostMapping("/services/{serviceId}/reviews")
    public ResponseEntity<ReviewResponseDTO> create(@PathVariable Long serviceId,
                                                    @Valid @RequestBody ReviewRequestDTO request) {
        ReviewResponseDTO created = reviewService.create(serviceId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/../{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<ReviewResponseDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody ReviewRequestDTO request) {
        return ResponseEntity.ok(reviewService.update(id, request));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
