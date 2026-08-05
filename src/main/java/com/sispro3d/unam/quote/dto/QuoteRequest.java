package com.sispro3d.unam.quote.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteRequest {
    private Long clientId;
    private Long offeredServiceId;
    private String description;
    private BigDecimal totalAmount;
    private LocalDate validUntil;
}
