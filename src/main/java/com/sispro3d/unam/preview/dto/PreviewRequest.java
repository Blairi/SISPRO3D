package com.sispro3d.unam.preview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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

    @NotNull(message = "{prev.NotNull.file}")
    private MultipartFile file;
}
