package com.sispro3d.unam.api.dto;

import jakarta.validation.constraints.NotBlank;
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
public class DeliverableRequestDTO {

    @NotBlank(message = "name is required")
    @Size(max = 60, message = "name must be at most 60 characters")
    private String name;

    @NotBlank(message = "urlFile is required")
    private String urlFile;

    @NotBlank(message = "fileType is required")
    @Size(max = 50, message = "fileType must be at most 50 characters")
    private String fileType;
}
