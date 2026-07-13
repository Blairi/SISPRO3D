package com.sispro3d.unam.workorder.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderRequest {
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer quoteId;
}
