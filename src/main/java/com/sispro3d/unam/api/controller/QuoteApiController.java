package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.QuoteDecisionDTO;
import com.sispro3d.unam.api.dto.QuoteReplyDTO;
import com.sispro3d.unam.api.dto.QuoteRequestDTO;
import com.sispro3d.unam.api.dto.QuoteResponseDTO;
import com.sispro3d.unam.api.service.ApiQuoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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

@RestController
@Validated
@RequestMapping("/api/v1/quotes")
public class QuoteApiController {

    private final ApiQuoteService quoteService;

    public QuoteApiController(ApiQuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public ResponseEntity<QuoteResponseDTO> create(@Valid @RequestBody QuoteRequestDTO request) {
        QuoteResponseDTO created = quoteService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponseDTO> findById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(quoteService.findById(id));
    }

    @PutMapping("/{id}/reply")
    public ResponseEntity<QuoteResponseDTO> reply(@PathVariable @Positive Long id,
                                                  @Valid @RequestBody QuoteReplyDTO request) {
        return ResponseEntity.ok(quoteService.reply(id, request));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<QuoteResponseDTO> accept(@PathVariable @Positive Long id,
                                                   @Valid @RequestBody QuoteDecisionDTO request) {
        return ResponseEntity.ok(quoteService.accept(id, request));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<QuoteResponseDTO> reject(@PathVariable @Positive Long id,
                                                   @Valid @RequestBody QuoteDecisionDTO request) {
        return ResponseEntity.ok(quoteService.reject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        quoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}