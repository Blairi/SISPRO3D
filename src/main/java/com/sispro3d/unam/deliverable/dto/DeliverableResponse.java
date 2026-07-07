package com.sispro3d.unam.deliverable.dto;

import com.sispro3d.unam.core.dto.WorkOrderRef;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverableResponse {
    private int id;
    private String name;
    private String urlFile;
    private String fileType;
    private LocalDateTime createdAt;
    private WorkOrderRef workOrder;
}
