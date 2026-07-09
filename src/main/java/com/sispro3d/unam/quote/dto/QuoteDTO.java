package com.sispro3d.unam.quote.dto;

import com.sispro3d.unam.core.dto.ClientRef;
import com.sispro3d.unam.core.dto.OfferedServiceRef;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteDTO {
    private int id;
    private String status; // PENDING, ACCEPTED, REJECTED, EXPIRED
    private BigDecimal totalAmount;
    private LocalDate validUntil;
    private String description;
    private LocalDateTime createdAt;
    private ClientRef client;
    private OfferedServiceRef offeredService;

    public QuoteDTO(int id) {
        this.id = id;
    }
}
