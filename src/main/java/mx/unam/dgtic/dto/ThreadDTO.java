package mx.unam.dgtic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadDTO {
    private int id;
    private WorkOrderDTO workOrder;

    public ThreadDTO(int id) {
        this.id = id;
    }
}
