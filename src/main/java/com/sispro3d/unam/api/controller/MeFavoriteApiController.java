package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.ServiceResponseDTO;
import com.sispro3d.unam.api.service.ApiFavoriteService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/me/favorite-services")
public class MeFavoriteApiController {

    private final ApiFavoriteService favoriteService;

    public MeFavoriteApiController(ApiFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{serviceId}")
    public ResponseEntity<ServiceResponseDTO> associate(@PathVariable @Positive Long serviceId) {
        ServiceResponseDTO associated = favoriteService.associateForCurrentUser(serviceId);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/services/{serviceId}")
                .buildAndExpand(serviceId)
                .toUri();
        return ResponseEntity.created(location).body(associated);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> list() {
        return ResponseEntity.ok(favoriteService.findForCurrentUser());
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> remove(@PathVariable @Positive Long serviceId) {
        favoriteService.removeForCurrentUser(serviceId);
        return ResponseEntity.noContent().build();
    }
}
