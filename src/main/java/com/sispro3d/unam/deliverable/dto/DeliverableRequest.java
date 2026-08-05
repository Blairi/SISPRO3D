package com.sispro3d.unam.deliverable.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverableRequest {
    private Long expertId;
    private Long workOrderId;
    private String name;
    private String urlFile;
    private String fileType;
}
