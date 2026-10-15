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
@RequestMapping("/api/v1/clients/{clientId}/favorite-services")
public class FavoriteApiController {

    private final ApiFavoriteService favoriteService;

    public FavoriteApiController(ApiFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{serviceId}")
    public ResponseEntity<ServiceResponseDTO> associate(@PathVariable @Positive Long clientId,
                                                        @PathVariable @Positive Long serviceId) {
        ServiceResponseDTO associated = favoriteService.associate(clientId, serviceId);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/services/{serviceId}")
                .buildAndExpand(serviceId)
                .toUri();
        return ResponseEntity.created(location).body(associated);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> findByClientId(@PathVariable @Positive Long clientId) {
        return ResponseEntity.ok(favoriteService.findByClientId(clientId));
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> remove(@PathVariable @Positive Long clientId, @PathVariable @Positive Long serviceId) {
        favoriteService.remove(clientId, serviceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/api/v1/me/favorite-services/{serviceId}", produces = "application/json")
    public ResponseEntity<ServiceResponseDTO> associateMe(@PathVariable @Positive Long serviceId) {
        ServiceResponseDTO associated = favoriteService.associateForCurrentUser(serviceId);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/services/{serviceId}")
                .buildAndExpand(serviceId)
                .toUri();
        return ResponseEntity.created(location).body(associated);
    }

    @GetMapping(value = "/api/v1/me/favorite-services", produces = "application/json")
    public ResponseEntity<List<ServiceResponseDTO>> findMe() {
        return ResponseEntity.ok(favoriteService.findForCurrentUser());
    }

    @DeleteMapping(value = "/api/v1/me/favorite-services/{serviceId}")
    public ResponseEntity<Void> removeMe(@PathVariable @Positive Long serviceId) {
        favoriteService.removeForCurrentUser(serviceId);
        return ResponseEntity.noContent().build();
    }
}