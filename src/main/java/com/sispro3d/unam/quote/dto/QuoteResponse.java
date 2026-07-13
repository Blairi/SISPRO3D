package com.sispro3d.unam.quote.dto;

import com.sispro3d.unam.core.dto.AccountRef;
import com.sispro3d.unam.core.dto.OfferedServiceRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    private int id;
    private String status;
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private LocalDateTime createdAt;
    private AccountRef client;
    private OfferedServiceRef offeredService;
}
