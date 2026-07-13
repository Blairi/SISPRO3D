package com.sispro3d.unam.workorder.domain;

import com.sispro3d.unam.quote.domain.Quote;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrder {
    private int id;
    private String status; // PENDING, IN_PROGRESS, IN_REVIEW, COMPLETED, CANCELED
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private Quote quote;
}
