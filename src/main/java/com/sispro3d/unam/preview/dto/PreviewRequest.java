package com.sispro3d.unam.preview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreviewRequest {
    private Long expertId;
    private Long deliverableId;
    private String caption;

    @NotBlank(message = "{prev.NotBlank.urlFile}")
    private String urlFile;
}
