package com.sispro3d.unam.preview.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreviewResponse {
    private Long id;
    private String caption;
    private String urlFile;
    private Long deliverableId;
    private LocalDateTime createdAt;
}
