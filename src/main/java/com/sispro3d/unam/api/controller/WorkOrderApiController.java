package com.sispro3d.unam.api.controller;

import com.sispro3d.unam.api.dto.WorkOrderResponseDTO;
import com.sispro3d.unam.api.dto.WorkOrderStatusRequestDTO;
import com.sispro3d.unam.api.service.ApiWorkOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/work-orders")
public class WorkOrderApiController {

    private final ApiWorkOrderService workOrderService;

    public WorkOrderApiController(ApiWorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.findById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<WorkOrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody WorkOrderStatusRequestDTO request) {
        return ResponseEntity.ok(workOrderService.updateStatus(id, request.getStatus()));
    }
}
