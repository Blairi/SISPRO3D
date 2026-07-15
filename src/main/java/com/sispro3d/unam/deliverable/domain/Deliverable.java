package com.sispro3d.unam.deliverable.domain;

import com.sispro3d.unam.workorder.domain.WorkOrder;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Deliverable {
    private int id;
    private String name;
    private String urlFile;
    private LocalDateTime createdAt;
    private String fileType;
    private WorkOrder workOrder;
}
