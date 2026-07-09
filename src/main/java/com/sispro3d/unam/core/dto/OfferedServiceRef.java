package com.sispro3d.unam.core.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferedServiceRef {
    private int id;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private int deliveryTimeDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
