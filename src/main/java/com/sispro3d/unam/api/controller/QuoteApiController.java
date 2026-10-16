package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.QuoteAcceptanceDTO;
import com.sispro3d.unam.api.dto.QuoteReplyDTO;
import com.sispro3d.unam.api.dto.QuoteRequestDTO;
import com.sispro3d.unam.api.dto.QuoteResponseDTO;
import com.sispro3d.unam.api.service.ApiQuoteService;
import com.sispro3d.unam.quote.domain.QuoteStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/quotes")
@Slf4j
public class QuoteApiController {

    private final ApiQuoteService quoteService;

    public QuoteApiController(ApiQuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public ResponseEntity<QuoteResponseDTO> create(@Valid @RequestBody QuoteRequestDTO request) {
        log.debug("POST /api/v1/quotes");
        QuoteResponseDTO created = quoteService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** Listado acotado a la identidad del token de acceso. */
    @GetMapping
    public ResponseEntity<List<QuoteResponseDTO>> findAll() {
        log.debug("GET /api/v1/quotes");
        return ResponseEntity.ok(quoteService.findAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<QuoteResponseDTO>> findByStatus(@PathVariable QuoteStatus status) {
        log.debug("GET /api/v1/quotes/status/{}", status);
        return ResponseEntity.ok(quoteService.findByStatus(status));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<QuoteResponseDTO>> findByService(@PathVariable @Positive Long serviceId) {
        log.debug("GET /api/v1/quotes/service/{}", serviceId);
        return ResponseEntity.ok(quoteService.findByService(serviceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponseDTO> findById(@PathVariable @Positive Long id) {
        log.debug("GET /api/v1/quotes/{}", id);
        return ResponseEntity.ok(quoteService.findById(id));
    }

    @PutMapping("/{id}/reply")
    public ResponseEntity<QuoteResponseDTO> reply(@PathVariable @Positive Long id,
                                                  @Valid @RequestBody QuoteReplyDTO request) {
        log.debug("PUT /api/v1/quotes/{}/reply", id);
        return ResponseEntity.ok(quoteService.reply(id, request));
    }

    /** Aceptar devuelve la cotización resuelta y la orden que crea. */
    @PutMapping("/{id}/accept")
    public ResponseEntity<QuoteAcceptanceDTO> accept(@PathVariable @Positive Long id) {
        log.debug("PUT /api/v1/quotes/{}/accept", id);
        return ResponseEntity.ok(quoteService.accept(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<QuoteResponseDTO> reject(@PathVariable @Positive Long id) {
        log.debug("PUT /api/v1/quotes/{}/reject", id);
        return ResponseEntity.ok(quoteService.reject(id));
    }

    @PutMapping("/{id}/expire")
    public ResponseEntity<QuoteResponseDTO> expire(@PathVariable @Positive Long id) {
        log.debug("PUT /api/v1/quotes/{}/expire", id);
        return ResponseEntity.ok(quoteService.expire(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        log.debug("DELETE /api/v1/quotes/{}", id);
        quoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
