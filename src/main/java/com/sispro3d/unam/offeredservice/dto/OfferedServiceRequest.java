package com.sispro3d.unam.offeredservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferedServiceRequest {

    @NotBlank(message = "{svc.NotBlank.title}")
    @Size(max = 255, message = "{svc.Size.title}")
    private String title;

    @Size(max = 5000, message = "{svc.Size.description}")
    private String description;

    @NotNull(message = "{svc.NotNull.basePrice}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{svc.DecimalMin.basePrice}")
    private BigDecimal basePrice;

    private Long expertId;
    private Long adminId;

    @NotNull(message = "{svc.NotNull.categoryId}")
    private Long categoryId;

    @NotNull(message = "{svc.NotNull.deliveryTimeDays}")
    @Min(value = 1, message = "{svc.Min.deliveryTimeDays}")
    @Max(value = 365, message = "{svc.Max.deliveryTimeDays}")
    private Integer deliveryTimeDays;
}
