package mx.unam.dgtic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverableDTO {
    private int id;
    private String name;
    private String urlFile;
    private LocalDateTime createdAt;
    private String fileType;
    private WorkOrderDTO workOrder;

    public DeliverableDTO(int id) {
        this.id = id;
    }
}
