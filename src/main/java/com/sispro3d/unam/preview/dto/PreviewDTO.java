package com.sispro3d.unam.preview.dto;

import com.sispro3d.unam.core.dto.DeliverableRef;
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
    private DeliverableRef deliverable;

    public PreviewDTO(int id) {
        this.id = id;
    }
}
