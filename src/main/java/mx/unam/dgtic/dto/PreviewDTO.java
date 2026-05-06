package mx.unam.dgtic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreviewDTO {
    private int id;
    private String caption;
    private String urlFile;
    private DeliverableDTO deliverable;

    public PreviewDTO(int id) {
        this.id = id;
    }
}
