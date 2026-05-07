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
public class PreviewDTO {
    private int id;
    private String caption;
    private String urlFile;
    private DeliverableDTO deliverable;

    public PreviewDTO(int id) {
        this.id = id;
    }
}
