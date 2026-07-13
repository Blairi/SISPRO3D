package com.sispro3d.unam.offeredservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferedServiceResponse {
    private int id;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int deliveryTimeDays;
}
