package com.sispro3d.unam.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverableResponseDTO {

    private Long id;
    private String name;
    private String urlFile;
    private String fileType;
    private LocalDateTime createdAt;
    private Long workOrderId;
}
