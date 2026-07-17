package com.sispro3d.unam.offeredservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferedServiceRequest {
    private String title;
    private String description;
    private BigDecimal basePrice;
    private Long expertId;
    private Long adminId;
    private Long categoryId;
    private Integer deliveryTimeDays;
}
