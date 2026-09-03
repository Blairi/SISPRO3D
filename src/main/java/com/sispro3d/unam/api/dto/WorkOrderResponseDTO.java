package com.sispro3d.unam.api.dto;

import com.sispro3d.unam.workorder.domain.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderResponseDTO {

    private Long id;
    private WorkOrderStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private Long quoteId;
    private Long clientId;
    private Long offeredServiceId;
}
