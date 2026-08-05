package com.sispro3d.unam.preview.dto;

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
    private String urlFile;
}
