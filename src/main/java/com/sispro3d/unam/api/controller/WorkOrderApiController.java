package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.WorkOrderCreateDTO;
import com.sispro3d.unam.api.dto.WorkOrderResponseDTO;
import com.sispro3d.unam.api.dto.WorkOrderStatusRequestDTO;
import com.sispro3d.unam.api.service.ApiWorkOrderService;
import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/work-orders")
@Slf4j
public class WorkOrderApiController {

    private final ApiWorkOrderService workOrderService;

    public WorkOrderApiController(ApiWorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    public ResponseEntity<WorkOrderResponseDTO> create(@Valid @RequestBody WorkOrderCreateDTO request) {
        log.debug("POST /api/v1/work-orders");
        WorkOrderResponseDTO created = workOrderService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** Listado acotado a la identidad del token de acceso. */
    @GetMapping
    public ResponseEntity<List<WorkOrderResponseDTO>> findAll() {
        log.debug("GET /api/v1/work-orders");
        return ResponseEntity.ok(workOrderService.findAll());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<WorkOrderResponseDTO>> findByClient(@PathVariable Long clientId) {
        log.debug("GET /api/v1/work-orders/client/{}", clientId);
        return ResponseEntity.ok(workOrderService.findByClient(clientId));
    }

    @GetMapping("/expert/{expertId}")
    public ResponseEntity<List<WorkOrderResponseDTO>> findByExpert(@PathVariable Long expertId) {
        log.debug("GET /api/v1/work-orders/expert/{}", expertId);
        return ResponseEntity.ok(workOrderService.findByExpert(expertId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<WorkOrderResponseDTO>> findByStatus(@PathVariable WorkOrderStatus status) {
        log.debug("GET /api/v1/work-orders/status/{}", status);
        return ResponseEntity.ok(workOrderService.findByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderResponseDTO> findById(@PathVariable Long id) {
        log.debug("GET /api/v1/work-orders/{}", id);
        return ResponseEntity.ok(workOrderService.findById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<WorkOrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderStatusRequestDTO request) {
        log.debug("PUT /api/v1/work-orders/{}/status - estado={}", id, request.getStatus());
        return ResponseEntity.ok(workOrderService.updateStatus(id, request.getStatus()));
    }
}
