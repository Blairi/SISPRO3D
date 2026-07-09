package com.sispro3d.unam.core.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRef {
    private int id;
    private String status;
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private LocalDateTime createdAt;
}
