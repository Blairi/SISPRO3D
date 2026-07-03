package com.sispro3d.unam.deliverable.domain;

import com.sispro3d.unam.workorder.domain.WorkOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deliverable {
    private int id;
    private String name;
    private String urlFile;
    private LocalDateTime createdAt;
    private String fileType;
    private WorkOrder workOrder;

    public Deliverable(int id) {
        this.id = id;
    }
}
