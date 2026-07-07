package com.sispro3d.unam.deliverable.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverableRequest {
    private String name;
    private String urlFile;
    private String fileType;
    private int workOrderId;
}
