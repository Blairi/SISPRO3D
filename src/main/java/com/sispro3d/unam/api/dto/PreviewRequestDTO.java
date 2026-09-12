package com.sispro3d.unam.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreviewRequestDTO {

    @NotNull(message = "expertId is required")
    private Long expertId;

    @NotBlank(message = "caption is required")
    @Size(max = 255, message = "caption must be at most 255 characters")
    private String caption;

    @NotBlank(message = "urlFile is required")
    @Size(max = 255, message = "urlFile must be at most 255 characters")
    private String urlFile;
}