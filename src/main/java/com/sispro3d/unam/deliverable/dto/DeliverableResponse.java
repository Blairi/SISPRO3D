package com.sispro3d.unam.deliverable.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverableResponse {
    private Long id;
    private String name;
    private String urlFile;
    private String fileType;
    private LocalDateTime createdAt;
    private Long workOrderId;
}
