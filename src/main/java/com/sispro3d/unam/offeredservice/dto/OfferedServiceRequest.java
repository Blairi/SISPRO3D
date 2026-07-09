package com.sispro3d.unam.offeredservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    private Integer adminId;
    private int expertId;
    private int categoryId;
    private int deliveryTimeDays;
}
