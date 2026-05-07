package mx.unam.dgtic.dto;

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
public class WorkOrderDTO {
    private int id;
    private String status; // PENDING, IN_PROGRESS, IN_REVIEW, COMPLETED, CANCELED
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private QuoteDTO quote;

    public WorkOrderDTO(int id) {
        this.id = id;
    }
}
