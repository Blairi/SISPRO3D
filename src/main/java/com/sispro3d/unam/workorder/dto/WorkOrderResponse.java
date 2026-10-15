package com.sispro3d.unam.workorder.dto;

import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderResponse {
    private Long id;
    private WorkOrderStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private Long quoteId;
    private Long clientId;
    private Long offeredServiceId;
}
