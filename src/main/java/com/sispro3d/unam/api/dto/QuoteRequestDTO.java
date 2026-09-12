package com.sispro3d.unam.api.dto;

import jakarta.validation.constraints.NotNull;
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
public class QuoteRequestDTO {

    @NotNull(message = "clientId is required")
    private Long clientId;

    @NotNull(message = "offeredServiceId is required")
    private Long offeredServiceId;

    private String description;
}