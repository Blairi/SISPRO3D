package com.sispro3d.unam.api.dto;

import com.sispro3d.unam.quote.domain.QuoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteResponseDTO {

    private Long id;
    private QuoteStatus status;
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private Long clientId;
    private Long offeredServiceId;
    private LocalDateTime createdAt;
}