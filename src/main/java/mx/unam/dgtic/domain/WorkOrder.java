package mx.unam.dgtic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrder {
    private int id;
    private String status; // PENDING, IN_PROGRESS, IN_REVIEW, COMPLETED, CANCELED
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private Quote quote;

    public WorkOrder(int id) {
        this.id = id;
    }
}