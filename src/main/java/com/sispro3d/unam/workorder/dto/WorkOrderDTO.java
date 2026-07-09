package com.sispro3d.unam.workorder.dto;

import com.sispro3d.unam.core.dto.QuoteRef;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDTO {
    private int id;
    private String status; // PENDING, IN_PROGRESS, IN_REVIEW, COMPLETED, CANCELED
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private QuoteRef quote;

    public WorkOrderDTO(int id) {
        this.id = id;
    }
}
