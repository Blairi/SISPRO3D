package com.sispro3d.unam.quote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteRequest {
    private String status;
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private int clientId;
    private int offeredServiceId;
}
