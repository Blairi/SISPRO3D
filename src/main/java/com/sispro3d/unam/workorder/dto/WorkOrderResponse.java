package com.sispro3d.unam.workorder.dto;

import com.sispro3d.unam.core.dto.QuoteRef;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderResponse {
    private int id;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private QuoteRef quote;
}
