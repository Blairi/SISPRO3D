package com.sispro3d.unam.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestDTO {

    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be at most 255 characters")
    private String title;

    @Size(max = 5000, message = "description must be at most 5000 characters")
    private String description;

    @NotNull(message = "basePrice is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "basePrice must be positive")
    private BigDecimal basePrice;

    @NotNull(message = "expertId is required")
    private Long expertId;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotNull(message = "deliveryTimeDays is required")
    @Min(value = 1, message = "deliveryTimeDays must be at least 1")
    @Max(value = 365, message = "deliveryTimeDays must be at most 365")
    private Integer deliveryTimeDays;
}
