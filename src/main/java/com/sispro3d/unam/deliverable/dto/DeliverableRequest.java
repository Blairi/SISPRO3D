package com.sispro3d.unam.deliverable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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

    @NotNull(message = "{del.NotNull.file}")
    private MultipartFile file;
}
