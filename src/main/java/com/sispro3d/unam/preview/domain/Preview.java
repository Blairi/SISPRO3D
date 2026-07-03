package com.sispro3d.unam.preview.domain;

import com.sispro3d.unam.deliverable.domain.Deliverable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Preview {
    private int id;
    private String caption;
    private String urlFile;
    private Deliverable deliverable;

    public Preview(int id) {
        this.id = id;
    }
}
