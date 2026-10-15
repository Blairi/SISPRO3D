package com.sispro3d.unam.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    @NotNull(message = "{rev.NotNull.rating}")
    @Min(value = 1, message = "{rev.Min.rating}")
    @Max(value = 5, message = "{rev.Max.rating}")
    private Integer rating;

    @Size(max = 2000, message = "{rev.Size.comment}")
    private String comment;
    private Long clientId;
    private Long offeredServiceId;
}
