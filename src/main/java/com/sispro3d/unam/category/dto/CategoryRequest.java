package com.sispro3d.unam.category.dto;

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
public class CategoryRequest {

    @NotBlank(message = "{cat.NotBlank.name}")
    @Size(max = 50, message = "{cat.Size.name}")
    private String name;

    @Size(max = 1000, message = "{cat.Size.description}")
    private String description;
}
