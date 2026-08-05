package com.sispro3d.unam.quote.dto;

import com.sispro3d.unam.quote.domain.QuoteStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteResponse {
    private Long id;
    private QuoteStatus status;
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private Long clientId;
    private Long offeredServiceId;
    private LocalDateTime createdAt;
}
