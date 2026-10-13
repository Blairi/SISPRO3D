package com.sispro3d.unam.deliverable.dto;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "{del.NotBlank.name}")
    private String name;

    @NotBlank(message = "{del.NotBlank.urlFile}")
    private String urlFile;

    @NotBlank(message = "{del.NotBlank.fileType}")
    private String fileType;
}
